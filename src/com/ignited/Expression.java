package com.ignited;

import java.util.LinkedList;
import java.util.Stack;
import java.util.logging.Logger;

public class Expression {

    private static final Logger LOGGER = Logger.getLogger(Expression.class.getName());

    private String exp;

    public Expression(String exp) {
        this.exp = exp;
        initStack(exp);
        //LOGGER.info(comparePriority('*', '+') + ", " + comparePriority('+', '*'));
    }

    private void initStack(String exp){

        LinkedStack<Object> stack = new LinkedStack<>();
        LinkedStack<Character> operatorStack = new LinkedStack<>();
        StringBuilder numberBuilder = new StringBuilder();

        boolean flag = false;
        for (int i = 0;i<exp.length();++i) {
            char c = exp.charAt(i);

            if ((c >= '0' && c <= '9') || c == '.') {
                numberBuilder.append(c);
                flag = true;
            } else {
                if (!flag && (c == '+' || c == '-')) {
                    numberBuilder.append(c);
                    flag = true;
                } else {
                    switch (c) {
                        case '+':
                        case '-':
                        case '*':
                        case '/':
                            flag = false;
                            numberPush(stack, numberBuilder.toString());
                            numberBuilder = new StringBuilder();

                            while (!operatorStack.isEmpty()) {
                                char prev = operatorStack.poll();
                                int compare = comparePriority(prev, c);
                                if (compare >= 0) {
                                    stack.push(operatorStack.pop());
                                } else {
                                    break;
                                }
                            }


                            operatorStack.push(c);
                    }
                }
            }
        }
        double v;

        try {
            v = Double.parseDouble(numberBuilder.toString());
        } catch (NumberFormatException e) {
            throw new ExpressionFormatException("Parsing number failed (number=" + numberBuilder + ", expression=" + exp + ")", e);
        }
        stack.push(v);
        while (!operatorStack.isEmpty()) {
            stack.push(operatorStack.pop());
        }

        LOGGER.info(String.valueOf(stack.top));
    }

    private void numberPush(LinkedStack<Object> stack, String value){
        double v;

        try {
            v = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new ExpressionFormatException("Parsing number failed (number=" + value + ", expression=" + exp + ")", e);
        }
        stack.push(v);

    }

    private int comparePriority(char o1, char o2){
        int i1 = getPriority(o1);
        int i2 = getPriority(o2);
        return Integer.compare(i1, i2);
    }

    private int getPriority(char c){
        switch (c){
            case '\0' :
                return Integer.MAX_VALUE;
            case '*' : case '/' :
                return 5;
            case '+' : case '-' :
                return 3;
            default:
                return -1;
        }
    }


    private static class LinkedStack<E> {

        Node<E> top;

        public LinkedStack() { }

        public boolean isEmpty() {
            return top == null;
        }

        public void push(E e){
            top = new Node<E>(e, top);
        }

        public E pop() {
            if(top == null){
                return null;
            }
            E data = top.data;
            top = top.link;
            return data;
        }

        public E poll(){
            if(top == null){
                return null;
            }
            return top.data;
        }

        static class Node<E> {
            E data;
            Node<E> link;

            public Node(E data, Node<E> link) {
                this.data = data;
                this.link = link;
            }

            @Override
            public String toString() {
                return "Node{" +
                        "data=" + data +
                        ", link=" + link +
                        '}';
            }
        }

    }
}
