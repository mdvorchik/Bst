package org.example;

import java.util.*;

import static org.example.State.*;

public class BstSeq {
    static class NodeSeq {
        private State state;
        private Integer value;
        private NodeSeq left;
        private NodeSeq right;

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }

        public NodeSeq getLeft() {
            return left;
        }

        public void setLeft(NodeSeq left) {
            this.left = left;
        }

        public NodeSeq getRight() {
            return right;
        }

        public void setRight(NodeSeq right) {
            this.right = right;
        }

        public State getState() {
            return state;
        }

        public void setState(State state) {
            this.state = state;
        }
    }

    private final NodeSeq root;

    public BstSeq(Integer initialVal) {
        this.root = new NodeSeq();
        this.root.setValue(initialVal);
        this.root.setState(DATA);
    }

    private List<NodeSeq> traversal(Integer v) {
        NodeSeq gPrev = new NodeSeq();
        NodeSeq prev = new NodeSeq();
        NodeSeq curr = this.root;

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
        }

        return Arrays.asList(gPrev, prev, curr);
    }

    public boolean contains(Integer v) {
        List<NodeSeq> traversal = traversal(v);
        NodeSeq curr = traversal.get(2);
        return curr != null && curr.getState().equals(DATA);
    }

    public boolean insert(Integer v) {
        List<NodeSeq> traversal = traversal(v);
        NodeSeq prev = traversal.get(1);
        NodeSeq curr = traversal.get(2);

        if (curr != null) {
            if (curr.getState().equals(DATA)) {
                return false;
            }

            curr.setState(DATA);
        } else {
            NodeSeq newNodeSeq = new NodeSeq();
            newNodeSeq.setValue(v);
            newNodeSeq.setState(DATA);
            if (prev.getValue().compareTo(v) > 0) {
                prev.setLeft(newNodeSeq);
            } else {
                prev.setRight(newNodeSeq);
            }
        }

        return true;
    }

    public boolean delete(Integer v) {
        List<NodeSeq> traversal = traversal(v);
        NodeSeq gPrev = traversal.get(0);
        NodeSeq prev = traversal.get(1);
        NodeSeq curr = traversal.get(2);

        if (Objects.isNull(curr) || !curr.getState().equals(DATA)) {
            return false;
        }

        if (curr.getLeft() != null && curr.getRight() != null) {
            curr.setState(ROUTING);
        } else if (curr.getLeft() != null || curr.getRight() != null) {
            NodeSeq child = curr.getLeft() != null ? curr.getLeft() : curr.getRight();

            if (curr.getValue().compareTo(prev.getValue()) < 0) {
                prev.setLeft(child);
            } else {
                prev.setRight(child);
            }
        } else {
            if (prev.getState().equals(DATA)) {
                if (curr == prev.getLeft()) {
                    prev.setLeft(null);
                } else {
                    prev.setRight(null);
                }
            } else {
                NodeSeq child;
                if (curr == prev.getLeft()) {
                    child = prev.getRight();
                } else {
                    child = prev.getLeft();
                }

                if (prev == gPrev.getLeft()) {
                    gPrev.setLeft(child);
                } else {
                    gPrev.setRight(child);
                }
            }
        }

        return true;
    }

    public List<Integer> inorderTraversal() {
        List<Integer> list = new ArrayList<>();
        Stack<NodeSeq> stack = new Stack<>();
        NodeSeq curr = root;
        while (curr != null || !stack.empty()) {
            while (curr != null) {
                stack.push(curr);
                curr = curr.getLeft();
            }
            curr = stack.pop();
            if (curr.getState().equals(DATA)) {
                list.add(curr.getValue());
            }
            curr = curr.getRight();
        }
        return list;
    }
}