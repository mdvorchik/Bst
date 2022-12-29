package org.example;

import java.util.*;
import java.util.concurrent.locks.Lock;

import static org.example.State.DATA;
import static org.example.State.ROUTING;

public class BstPar {
    private final NodePar root;
    private static final ThreadLocal<Stack<Lock>> localLocks = ThreadLocal.withInitial(Stack::new);

    public BstPar(Integer initialVal) {
        this.root = new NodePar();
        this.root.setValue(initialVal);
        this.root.setState(DATA);
    }

    private List<NodePar> traversal(Integer v) {
        while (true) {
            NodePar gPrev = new NodePar();
            NodePar prev = new NodePar();
            NodePar curr = this.root;

            while (curr != null) {
                if (curr.getValue().equals(v)) {
                    break;
                } else {
                    gPrev = prev;
                    prev = curr;
                    if (curr.getValue().compareTo(v) > 0) {
                        curr = curr.getLeft();
                    } else {
                        curr = curr.getRight();
                    }
                }

                if (checkDeleted(gPrev)) break;
                if (checkDeleted(prev)) break;
                if (checkDeleted(curr)) break;
            }

            if (checkDeleted(gPrev)) continue;
            if (checkDeleted(prev)) continue;
            if (checkDeleted(curr)) continue;

            return Arrays.asList(gPrev, prev, curr);
        }
    }

    private boolean checkDeleted(NodePar node) {
        return node != null && node.getDeleted().get();
    }

    public boolean contains(Integer v) {
        List<NodePar> traversal = traversal(v);
        NodePar curr = traversal.get(2);
        return curr != null && curr.getState().equals(DATA);
    }

    public boolean insert(Integer v) {
        while (true) {
            try {
                List<NodePar> traversal = traversal(v);
                NodePar prev = traversal.get(1);
                NodePar curr = traversal.get(2);

                if (curr != null) {
                    if (curr.getState().equals(DATA)) {
                        return false;
                    }
                    curr.tryWriteLockState(ROUTING, localLocks);
                    curr.setState(DATA);
                } else {
                    NodePar newNodePar = new NodePar();
                    newNodePar.setValue(v);
                    newNodePar.setState(DATA);
                    if (prev.getValue().compareTo(v) > 0) {
                        prev.tryReadLockState(localLocks);
                        prev.tryLockLeftEdgeRef(null, localLocks);
                        prev.setLeft(newNodePar);
                    } else {
                        prev.tryReadLockState(localLocks);
                        prev.tryLockRightEdgeRef(null, localLocks);
                        prev.setRight(newNodePar);
                    }
                }

                return true;
            } catch (Exception ignore) {
            } finally {
                unlockAllLocks();
            }
        }
    }

    private void unlockAllLocks() {
        while (!localLocks.get().empty()) {
            localLocks.get().pop().unlock();
        }
    }

    public boolean delete(Integer v) {
        while (true) {
            try {
                List<NodePar> traversal = traversal(v);
                NodePar gPrev = traversal.get(0);
                NodePar prev = traversal.get(1);
                NodePar curr = traversal.get(2);

                if (curr == null || !curr.getState().equals(DATA)) {
                    return false;
                }

                if (curr.getLeft() != null && curr.getRight() != null) {
                    curr.tryWriteLockState(DATA, localLocks);

                    if (curr.getLeft() == null || curr.getRight() == null) {
                        throw new RuntimeException("try delete but curr does not have 2 children");
                    }
                    curr.setState(ROUTING);
                } else if (curr.getLeft() != null || curr.getRight() != null) {
                    NodePar child = curr.getLeft() != null ? curr.getLeft() : curr.getRight();

                    if (curr.getValue().compareTo(prev.getValue()) < 0) {
                        lockVertexWithOneChild(prev, curr, child);
                        curr.getDeleted().set(true);
                        prev.setLeft(child);
                    } else {
                        lockVertexWithOneChild(prev, curr, child);
                        curr.getDeleted().set(true);
                        prev.setRight(child);
                    }
                } else {
                    if (prev.getState().equals(DATA)) {
                        if (curr.getValue().compareTo(prev.getValue()) < 0) {
                            prev.tryReadLockState(DATA, localLocks);

                            curr = lockLeaf(v, prev, curr);

                            curr.getDeleted().set(true);
                            prev.setLeft(null);
                        } else {
                            prev.tryReadLockState(DATA, localLocks);

                            curr = lockLeaf(v, prev, curr);

                            curr.getDeleted().set(true);
                            prev.setRight(null);
                        }
                    } else {
                        NodePar child;
                        if (curr.getValue().compareTo(prev.getValue()) < 0) {
                            child = prev.getRight();
                        } else {
                            child = prev.getLeft();
                        }

                        if (gPrev.getLeft() != null && prev == gPrev.getLeft()) {
                            gPrev.tryLockEdgeRef(prev, localLocks);
                            prev.tryWriteLockState(ROUTING, localLocks);
                            prev.tryLockEdgeRef(child, localLocks);

                            curr = lockLeaf(v, prev, curr);

                            prev.getDeleted().set(true);
                            curr.getDeleted().set(true);
                            gPrev.setLeft(child);
                        } else if (gPrev.getRight() != null && prev == gPrev.getRight()) {
                            gPrev.tryLockEdgeRef(prev, localLocks);
                            prev.tryWriteLockState(ROUTING, localLocks);
                            prev.tryLockEdgeRef(child, localLocks);

                            curr = lockLeaf(v, prev, curr);

                            prev.getDeleted().set(true);
                            curr.getDeleted().set(true);
                            gPrev.setRight(child);
                        }
                    }
                }
                return true;
            } catch (Exception ignore) {
            } finally {
                unlockAllLocks();
            }
        }
    }

    private NodePar lockLeaf(Integer v, NodePar prev, NodePar curr) {
        prev.tryLockEdgeVal(curr, localLocks);

        if (v.compareTo(prev.getValue()) < 0) {
            curr = prev.getLeft();
        } else {
            curr = prev.getRight();
        }

        curr.tryWriteLockState(DATA, localLocks);

        if (curr.getLeft() != null || curr.getRight() != null) {
            throw new RuntimeException("try lockLeaf but it is not leaf");
        }
        return curr;
    }

    private void lockVertexWithOneChild(NodePar prev, NodePar curr, NodePar child) {
        prev.tryLockEdgeRef(curr, localLocks);
        curr.tryWriteLockState(DATA, localLocks);

        if (curr.getLeft() != null && curr.getRight() != null) {
            throw new RuntimeException("try lockVertexWithOneChild but has 2 children");
        }

        if (curr.getLeft() == null && curr.getRight() == null) {
            throw new RuntimeException("try lockVertexWithOneChild but has 0 children");
        }

        curr.tryLockEdgeRef(child, localLocks);
    }

    public List<Integer> inorderTraversal() {
        List<Integer> list = new ArrayList<>();
        Stack<NodePar> stack = new Stack<>();
        NodePar curr = root;
        while (curr != null || !stack.empty()) {
            while (curr != null) {
                stack.push(curr);
                curr = curr.getLeft();
            }
            curr = stack.pop();
            if (curr.getState().equals(DATA) && !curr.getDeleted().get()) {
                list.add(curr.getValue());
            }
            curr = curr.getRight();
        }
        return list;
    }
}