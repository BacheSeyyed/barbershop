import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class BarberShop {

    static Semaphore max_capacity = new Semaphore(20,true);
    static Semaphore sofa = new Semaphore(4,true);
    static Semaphore barber_chair = new Semaphore(3,true);
    static Semaphore coord = new Semaphore(3,true);
    static Semaphore mutex1 = new Semaphore(1,true);
    static Semaphore mutex2 = new Semaphore(1,true);
    static Semaphore cust_ready = new Semaphore(0,true);
    static Semaphore leave_barber_chair = new Semaphore(0,true);
    static Semaphore payment = new Semaphore(0,true);
    static Semaphore receipt = new Semaphore(0,true);
    static Semaphore finished[] = new Semaphore[50];
    static Queue<Integer> queue = new LinkedList<>();
    static int count;


    class Customer extends Thread {
        private int number;

        public Customer() throws InterruptedException {
            mutex1.acquire();
            this.number = count;
            count++;
            mutex1.release();
        }

        public int getNumber() {
            return number;
        }

        @Override
        public void run() {

            try {
                max_capacity.acquire();
                System.out.println(getNumber() + " enter shop");
                delay();// eneter shop
                sofa.acquire();
                System.out.println(number + " sits on sofa");
                delay();//sit on sofa
                barber_chair.acquire();
                System.out.println(number + " sits in barber chair");
                delay();//get up from sofa
                sofa.release();
                delay();//sit in barber chair
                mutex2.acquire();
                queue.add(getNumber());
                cust_ready.release();
                mutex2.release();
                finished[getNumber()].acquire();
                System.out.println(number + " finish");
                delay();//leave barber chair
                leave_barber_chair.release();
                delay();//pay
                System.out.println(number+" pay");
                payment.release();
                receipt.acquire();
                System.out.println(number+" get receipt");
                delay();//exit from shop
                max_capacity.release();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        public void delay() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    class Barber extends Thread {
        private int b_cust;

        @Override
        public void run() {
            while (true) {
                try {

                    cust_ready.acquire();
                    mutex2.acquire();
                    b_cust = queue.remove();
                    mutex2.release();
                    coord.acquire();
                    delay();
                    coord.release();
                    finished[b_cust].release();
                    leave_barber_chair.acquire();
                    barber_chair.release();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void delay() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    class Cashier extends Thread {
        @Override
        public void run() {
            while (true) {
                try {

                    payment.acquire();
                    coord.acquire();
                    delay();//accep pay
                    coord.release();
                    receipt.release();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }

        public void delay() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    public void go() {
        count = 0;
        // initialize finished semaphor
        for (int i = 0; i < finished.length; i++)
            finished[i] = new Semaphore(0);

        Barber barber1 = new Barber();
        barber1.start();
        Barber barber2 = new Barber();
        barber2.start();
        Barber barber3 = new Barber();
        barber3.start();
        Cashier cashier = new Cashier();
        cashier.start();

        for (int i = 0; i < 50; i++) {
            try {

                Customer c=new Customer();
                c.start();
                Thread.sleep(500);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {
        BarberShop b=new BarberShop();
        b.go();
    }
}
