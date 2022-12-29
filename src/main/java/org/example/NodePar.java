package org.example;

import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class NodePar {
    private State state;
    private Integer value;
    private NodePar left;
    private NodePar right;

    private final AtomicBoolean deleted = new AtomicBoolean();
    private final ReentrantReadWriteLock leftLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock rightLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock stateLock = new ReentrantReadWriteLock();

    public void tryLockLeftEdgeRef(NodePar expRef, ThreadLocal<Stack<Lock>> localLocks) {
        ReentrantReadWriteLock.WriteLock lock = this.leftLock.writeLock();

        lock.lock();
        localLocks.get().push(lock);

        if (this.deleted.get() || this.left != expRef) {
            throw new RuntimeException("tryLockLeftEdgeRef error");
        }
    }

    public void tryLockRightEdgeRef(NodePar expRef, ThreadLocal<Stack<Lock>> localLocks) {
        ReentrantReadWriteLock.WriteLock lock = this.rightLock.writeLock();

        lock.lock();
        localLocks.get().push(lock);

        if (this.deleted.get() || this.right != expRef) {
            throw new RuntimeException("tryLockRightEdgeRef error");
        }
    }

    public void tryLockEdgeRef(NodePar expRef, ThreadLocal<Stack<Lock>> localLocks) {
        if (value.compareTo(expRef.getValue()) < 0) {
            tryLockRightEdgeRef(expRef, localLocks);
        } else {
            tryLockLeftEdgeRef(expRef, localLocks);
        }
    }

    public void tryLockLeftEdgeVal(Integer expVal, ThreadLocal<Stack<Lock>> localLocks) {
        ReentrantReadWriteLock.WriteLock lock = this.leftLock.writeLock();

        lock.lock();
        localLocks.get().push(lock);

        if (this.deleted.get() || Objects.isNull(left) || this.left.getValue().compareTo(expVal) != 0) {
            throw new RuntimeException("tryLockLeftEdgeVal error");
        }
    }

    public void tryLockRightEdgeVal(Integer expVal, ThreadLocal<Stack<Lock>> localLocks) {
        ReentrantReadWriteLock.WriteLock lock = this.rightLock.writeLock();

        lock.lock();
        localLocks.get().push(lock);

        if (this.deleted.get() || Objects.isNull(right) || this.right.getValue().compareTo(expVal) != 0) {
            throw new RuntimeException("tryLockRightEdgeVal error");
        }
    }

    public void tryLockEdgeVal(NodePar expRef, ThreadLocal<Stack<Lock>> localLocks) {
        if (value.compareTo(expRef.getValue()) < 0) {
            tryLockRightEdgeVal(expRef.getValue(), localLocks);
        } else {
            tryLockLeftEdgeVal(expRef.getValue(), localLocks);
        }
    }

    public void tryReadLockState(ThreadLocal<Stack<Lock>> localLocks) {
        ReentrantReadWriteLock.ReadLock lock = stateLock.readLock();

        lock.lock();
        localLocks.get().push(lock);

        if (deleted.get()) {
            throw new RuntimeException("tryReadLockState error");
        }
    }

    public void tryReadLockState(State expState, ThreadLocal<Stack<Lock>> localLocks) {
        ReentrantReadWriteLock.ReadLock lock = stateLock.readLock();

        lock.lock();
        localLocks.get().push(lock);

        if (deleted.get() || !expState.equals(state)) {
            throw new RuntimeException("tryReadLockState error");
        }
    }

    public void tryWriteLockState(State expState, ThreadLocal<Stack<Lock>> localLocks) {
        ReentrantReadWriteLock.WriteLock lock = stateLock.writeLock();

        lock.lock();
        localLocks.get().push(lock);

        if (deleted.get() || !expState.equals(state)) {
            throw new RuntimeException("tryWriteLockState error");
        }
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public NodePar getLeft() {
        return left;
    }

    public void setLeft(NodePar left) {
        this.left = left;
    }

    public NodePar getRight() {
        return right;
    }

    public void setRight(NodePar right) {
        this.right = right;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public AtomicBoolean getDeleted() {
        return deleted;
    }
}