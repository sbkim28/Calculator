package com.ignited;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

public class Expression {

    private static final Logger LOGGER = Logger.getLogger(Expression.class.getName());

    private String exp;
    private double value;

    public Expression(String exp) {
        this.exp = exp;
        value = calculateStack(initStack(parse(exp)));
        LOGGER.info(value + "");
    }

    private double calculateStack(LinkedList<Object> stack){
        LinkedStack<Double> numbers = new LinkedStack<>();

        while (!stack.isEmpty()){
            Object t = stack.pop();
            if(t instanceof Double){
                numbers.push((Double) t);

            }else if(t instanceof Character){
                double l;
                double f;
                try {
                    l = numbers.pop();
                    f = numbers.pop();
                }catch (NoSuchElementException e){
                    throw new ExpressionFormatException("Malformed Expression. (exp=" + exp + ")");
                }

                switch ((char) t){
                    case '+' :
                        numbers.push(f + l);
                        break;
                    case '-' :
                        numbers.push(f - l);
                        break;
                    case '*':
                        numbers.push(f * l);
                        break;
                    case '/':
                        numbers.push(f / l);
                        break;
                    case '^':
                        numbers.push(Math.pow(f, l));
                        break;
                    default:
                        throw new ExpressionFormatException("Illegal operator (operator=" + t + ")");
                }
            }
        }

        if(numbers.isEmpty())
            throw new ExpressionFormatException("Malformed Expression. (exp=" + exp + ")");

        double result = numbers.pop();

        if(!numbers.isEmpty())
            throw new ExpressionFormatException("Malformed Expression. (exp=" + exp + ")");

        return result;
    }

    private String parse(String exp){
        StringBuilder builder = new StringBuilder().append(exp);
        for (int i = 0;i<builder.length();++i){
            char c = builder.charAt(i);
            if(c == '-'){
                if(i == 0 || builder.charAt(i-1) < '0' || builder.charAt(i-1) > '9' ){
                    builder.insert(++i, "1*");
                    ++i;
                }
            }else if(c == '(' && i != 0){
                char n = builder.charAt(i - 1);
                if((n >= '0' && n <= '9') || n == ')' || n == '}' || n == ']'){
                    builder.insert(i++, "*");
                }
            }
        }
        LOGGER.info(builder.toString());
        return builder.toString();
    }

    private LinkedList<Object> initStack(String exp){
        LinkedList<Object> calc = new LinkedList<>();
        LinkedStack<Character> operatorStack = new LinkedStack<>();
        StringBuilder numberBuilder = new StringBuilder();

        boolean flag = false;
        for (int i = 0;i<exp.length();++i) {
            char c = exp.charAt(i);

            if ((c >= '0' && c <= '9') || c == '.') {
                numberBuilder.append(c);
                flag = true;
            } else {
                if (!flag && (c == '-')) {
                    numberBuilder.append(c);
                    flag = true;
                } else {
                    switch (c) {
                        case '(':
                        case '{':
                        case '[':
                            flag = false;
                            operatorStack.push(c);
                            break;

                        case ')':
                            if(numberBuilder.length() != 0) {
                                numberPush(calc, numberBuilder.toString());
                            }
                            numberBuilder = new StringBuilder();
                            bracketHandle(operatorStack, calc, '(');
                            flag = true;
                            break;
                        case '}':
                            if(numberBuilder.length() != 0) {
                                numberPush(calc, numberBuilder.toString());
                            }
                            numberBuilder = new StringBuilder();
                            bracketHandle(operatorStack, calc,'{');
                            flag = true;
                            break;
                        case ']':
                            if(numberBuilder.length() != 0) {
                                numberPush(calc, numberBuilder.toString());
                            }
                            numberBuilder = new StringBuilder();
                            bracketHandle(operatorStack, calc,'[');
                            flag = true;
                            break;

                        case '^':
                        case '+':
                        case '-':
                        case '*':
                        case '/':
                            flag = false;
                            if(numberBuilder.length() != 0) {
                                numberPush(calc, numberBuilder.toString());
                            }
                            numberBuilder = new StringBuilder();

                            while (!operatorStack.isEmpty()) {
                                char prev = operatorStack.poll();
                                int compare = comparePriority(prev, c);
                                if (compare >= 0) {
                                    calc.add(operatorStack.pop());
                                } else {
                                    break;
                                }
                            }

                            operatorStack.push(c);
                            break;


                            default:throw new ExpressionFormatException("Invalid operator");
                    }
                }
            }
        }

        if(numberBuilder.length() != 0) {
            double v;
            try {
                v = Double.parseDouble(numberBuilder.toString());
            } catch (NumberFormatException e) {
                throw new ExpressionFormatException("Parsing number failed (number=" + numberBuilder + ", expression=" + exp + ")", e);
            }
            calc.add(v);
        }
        while (!operatorStack.isEmpty()) {
            calc.add(operatorStack.pop());
        }

        LOGGER.info(String.valueOf(calc));
        return calc;
    }

    private void bracketHandle(LinkedStack<Character> operator, LinkedList<Object> calc, char until){
        char c;
        while (!operator.isEmpty()
                && (c = operator.pop()) != until){
            calc.add(c);
        }
    }

    private void numberPush(LinkedList<Object> calc, String value){
        double v;

        try {
            v = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new ExpressionFormatException("Parsing number failed (number=" + value + ", expression=" + exp + ")", e);
        }
        calc.add(v);

    }

    private int comparePriority(char o1, char o2){
        int i1 = getPriority(o1);
        int i2 = getPriority(o2);
        return Integer.compare(i1, i2);
    }

    private int getPriority(char c){
        switch (c){
            case '(': case '{' : case '[' :
                case ')' : case '}' : case ']' :
                return 1;
            case '^' :
                return 7;
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
                throw new NoSuchElementException("Empty");
            }
            E data = top.data;
            top = top.link;
            return data;
        }

        public E poll(){
            if(top == null){
                throw new NoSuchElementException("Empty");
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
