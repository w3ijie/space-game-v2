package com.example.space_game_v2.feature.game.logic;

import android.util.Log;

import com.example.space_game_v2.feature.game.elements.Spaceship;

import java.util.ArrayList;
import java.util.List;

public class Queue {
    private volatile Spaceship[] queue;
    private volatile int front = 0;
    private volatile int back = 0;
    private volatile int itemCount = 0;

    // constructor
    public Queue(int size) {
        queue = new Spaceship[size];
    }

    // method to add an order to the queue
    synchronized void add(Spaceship spaceship) {
        while (itemCount == queue.length) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                Log.e("Queue", "Error", e);
            }
        }
        queue[back] = spaceship;
        back = (back + 1) % queue.length;
        itemCount++;
        this.notifyAll();
    }

    // method to remove an order from the queue
    synchronized Spaceship remove() {

        // wait if the queue is empty
        while (itemCount == 0) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                Log.e("Ship Producer", "Error", e);
            }
        }

        // otherwise remove the order from the front of the queue
        final Spaceship spaceship = queue[front];
        queue[front] = null;

        // increment the front index
        front = (front + 1) % queue.length;
        itemCount--;

        this.notifyAll();

        return spaceship;
    }

    // method to check if the queue is empty
    synchronized boolean isEmpty() {
        return itemCount == 0;
    }

    // method to get the number of items in the queue
    synchronized int size() {
        return itemCount;
    }

    // method to get max size of the queue
    synchronized int maxSize() {
        return queue.length;
    }

    synchronized List<Spaceship> getAll() {
        List<Spaceship> list = new ArrayList<>();
        int index = front;
        for (int i = 0; i < itemCount; i++) {
            list.add(queue[index]);
            index = (index + 1) % queue.length;
        }
        return list;
    }

    synchronized void clear() {
        for (int i = 0; i < queue.length; i++) {
            queue[i] = null;
        }
        front = 0;
        back = 0;
        itemCount = 0;
        this.notifyAll();
    }
}
