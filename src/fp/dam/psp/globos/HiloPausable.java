package fp.dam.psp.globos;

import java.util.concurrent.locks.ReentrantLock;

public abstract class HiloPausable extends Thread{

    protected boolean pausado;
    ReentrantLock lock = new ReentrantLock();

    public HiloPausable(String nombre) {
        super(nombre);
    }


    // añadido lock
    public void pausaOnOff() {
        this.lock.lock();
        pausado = !pausado;
        if (!pausado)
            notifyAll();
        this.lock.unlock();
    }

    // añadido lock
    protected void checkPausa() {
        this.lock.lock();
        if (pausado) {
            try {
                wait();
            } catch (InterruptedException e) {
                interrupt();
            }
        }
        this.lock.unlock();
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            checkPausa();
            if (!isInterrupted())
                tarea();
        }
    }

    protected abstract void tarea();
}
