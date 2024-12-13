package fp.dam.psp.globos;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

public class Deposito extends HiloPausable {

    private final ArrayList<Globo> deshinchados = new ArrayList<>();
    private final ArrayList<Globo> hinchando = new ArrayList<>();
    private final int maxGlobos;
    private final int maxH;
    private final Consola consola;
    private int total = 0;
    private int explotados = 0;
    private int pinchados = 0;
    ReentrantLock lock = new ReentrantLock();


    public Deposito(int maxGlobos, int maxH, Consola consola) {
        super("REPONEDOR");
        this.maxGlobos = maxGlobos;
        this.maxH = maxH;
        this.consola = consola;
    }



    public synchronized void reponer() {
        while (deshinchados.size() + hinchando.size() == maxGlobos) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        Globo globo = new Globo(++total, 5, consola);
        deshinchados.add(globo);
        consola.actualizarTotalGlobos(total, deshinchados.size(), hinchando.size());
        notifyAll();
    }


    public synchronized Globo getDeshinchado() {
        while (deshinchados.isEmpty() || hinchando.size() == maxH) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        Globo globo = deshinchados.removeFirst();
        globo.setHinchando();
        hinchando.add(globo);
        consola.println(globo.getNombre() + " ENTREGADO A " + Thread.currentThread().getName());
        consola.actualizarGlobosDepositados(deshinchados.size(), hinchando.size());
        notifyAll();
        return globo;
    }


    // added lock
    public synchronized void retirar(Globo globo) {
        this.lock.lock();
        hinchando.remove(globo);
        notifyAll();
        this.lock.unlock();
    }

    private final Random r = new Random();

    // added lock
    public synchronized Globo getHinchando() {
        this.lock.lock();
        while (hinchando.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        Globo globo = hinchando.get(r.nextInt(hinchando.size()));
        notifyAll();
        this.lock.unlock();
        return globo;
    }

    @Override
    protected void tarea() {
        reponer();
    }
}
