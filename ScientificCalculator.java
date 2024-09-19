import java.awt.*;
import java.awt.event.*;
import java.util.Stack;
import javax.swing.*;

public class ScientificCalculator extends JFrame implements ActionListener {
    private JTextField display;
    private JPanel panel;

    private String[] buttonLabels = {
        "Clear", "(", ")", "/", "sqrt",
        "7", "8", "9", "*", "^",
        "4", "5", "6", "-", "log",
        "1", "2", "3", "+", "ln",
        "0", ".", "=", "sin", "cos",
        "tan", "(-)", "π", "e", "Inv"
    };

    private JButton[] buttons = new JButton[buttonLabels.length];
    private StringBuilder input = new StringBuilder();
    private boolean inverseMode = false; // Tracks whether inverse mode is active

    public ScientificCalculator() {
        setTitle("Scientific Calculator");
        setSize(500, 700); // Increased size to accommodate new buttons
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create display field
        display = new JTextField();
        display.setEditable(false);
        display.setFont(new Font("Arial", Font.BOLD, 24));
        display.setHorizontalAlignment(JTextField.RIGHT); // Align text to the right
        add(display, BorderLayout.NORTH);

        // Create panel for buttons
        panel = new JPanel();
        panel.setLayout(new GridLayout(7, 5, 5, 5)); // 7 rows, 5 columns

        // Set preferred size for buttons
        Dimension buttonSize = new Dimension(90, 60);

        // Add buttons to the panel
        for (int i = 0; i < buttonLabels.length; i++) {
            buttons[i] = new JButton(buttonLabels[i]);
            buttons[i].setPreferredSize(buttonSize);
            buttons[i].setFont(new Font("Arial", Font.BOLD, 16));
            buttons[i].addActionListener(this);
            panel.add(buttons[i]);
        }

        add(panel, BorderLayout.CENTER);
    }

    // Handle button clicks
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (command.equals("Clear")) {
            input.setLength(0); // Clear the input
            display.setText(""); // Clear the display
            inverseMode = false; // Reset inverse mode
            updateFunctionButtons(); // Reset function button labels
        } else if (command.equals("=")) {
            try {
                double result = evaluateExpression(input.toString());
                display.setText(Double.toString(result));
                input.setLength(0); // Clear input after evaluation
            } catch (Exception ex) {
                display.setText("Error");
                input.setLength(0);
            }
        } else if (command.equals("Inv")) {
            inverseMode = !inverseMode; // Toggle inverse mode
            updateFunctionButtons();
        } else if (command.equals("(-)")) {
            input.append("(-1)*");
            display.setText(input.toString());
        } else if (command.equals("π")) {
            input.append("pi");
            display.setText(input.toString());
        } else if (command.equals("e")) {
            input.append("e");
            display.setText(input.toString());
        } else if (isFunctionButton(command)) {
            // Handle function buttons
            String functionToken = getFunctionToken(command);
            input.append(functionToken + "(");
            display.setText(input.toString());
        } else {
            input.append(command);
            display.setText(input.toString());
        }
    }

    // Update function button labels based on inverse mode
    private void updateFunctionButtons() {
        for (int i = 0; i < buttons.length; i++) {
            String label = buttons[i].getText();
            if (label.equals("sin") || label.equals("asin")) {
                buttons[i].setText(inverseMode ? "asin" : "sin");
            } else if (label.equals("cos") || label.equals("acos")) {
                buttons[i].setText(inverseMode ? "acos" : "cos");
            } else if (label.equals("tan") || label.equals("atan")) {
                buttons[i].setText(inverseMode ? "atan" : "tan");
            } else if (label.equals("log") || label.equals("10^x")) {
                buttons[i].setText(inverseMode ? "10^x" : "log");
            } else if (label.equals("ln") || label.equals("e^x")) {
                buttons[i].setText(inverseMode ? "e^x" : "ln");
            }
        }
    }

    // Evaluate the mathematical expression
    private double evaluateExpression(String expression) throws Exception {
        // Replace functions with single-letter tokens
        expression = expression.replaceAll("asin", "S");
        expression = expression.replaceAll("acos", "C");
        expression = expression.replaceAll("atan", "T");
        expression = expression.replaceAll("10\\^x", "G");
        expression = expression.replaceAll("e\\^x", "E");
        expression = expression.replaceAll("sin", "s");
        expression = expression.replaceAll("cos", "c");
        expression = expression.replaceAll("tan", "t");
        expression = expression.replaceAll("log", "g");
        expression = expression.replaceAll("ln", "l");
        expression = expression.replaceAll("sqrt", "q");
        expression = expression.replaceAll("pi", String.valueOf(Math.PI));
        expression = expression.replaceAll("\\be\\b", String.valueOf(Math.E));

        // Convert infix expression to postfix notation
        String postfix = infixToPostfix(expression);

        // Evaluate the postfix expression
        double result = evaluatePostfix(postfix);

        return result;
    }

    // Helper method to convert infix to postfix notation
    private String infixToPostfix(String infix) throws Exception {
        StringBuilder postfix = new StringBuilder();
        Stack<Character> stack = new Stack<>();
        int i = 0;
        while (i < infix.length()) {
            char c = infix.charAt(i);

            if (Character.isDigit(c) || c == '.') {
                // Parse numbers
                while (i < infix.length() && (Character.isDigit(infix.charAt(i)) || infix.charAt(i) == '.')) {
                    postfix.append(infix.charAt(i));
                    i++;
                }
                postfix.append(' ');
            } else if (isFunction(c)) {
                stack.push(c);
                i++;
            } else if (c == '(') {
                stack.push(c);
                i++;
            } else if (c == ')') {
                while (!stack.isEmpty() && stack.peek() != '(') {
                    postfix.append(stack.pop()).append(' ');
                }
                if (stack.isEmpty()) {
                    throw new Exception("Mismatched parentheses");
                }
                stack.pop(); // Remove '('
                if (!stack.isEmpty() && isFunction(stack.peek())) {
                    postfix.append(stack.pop()).append(' ');
                }
                i++;
            } else if (isOperator(c)) {
                while (!stack.isEmpty() && precedence(stack.peek()) >= precedence(c)) {
                    postfix.append(stack.pop()).append(' ');
                }
                stack.push(c);
                i++;
            } else {
                throw new Exception("Invalid character: " + c);
            }
        }
        while (!stack.isEmpty()) {
            char top = stack.pop();
            if (top == '(' || top == ')') {
                throw new Exception("Mismatched parentheses");
            }
            postfix.append(top).append(' ');
        }
        return postfix.toString();
    }

    private boolean isFunction(char c) {
        return c == 's' || c == 'c' || c == 't' || c == 'g' || c == 'l' || c == 'q'
            || c == 'S' || c == 'C' || c == 'T' || c == 'G' || c == 'E';
    }

    private boolean isFunctionButton(String label) {
        return label.equals("sin") || label.equals("cos") || label.equals("tan")
            || label.equals("asin") || label.equals("acos") || label.equals("atan")
            || label.equals("log") || label.equals("ln") || label.equals("10^x") || label.equals("e^x")
            || label.equals("sqrt");
    }

    private String getFunctionToken(String label) {
        return label;
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '^';
    }

    private int precedence(char op) {
        switch (op) {
            case '^':
                return 4;
            case '*':
            case '/':
                return 3;
            case '+':
            case '-':
                return 2;
            default:
                return 0;
        }
    }

    // Helper method to evaluate postfix expression
    private double evaluatePostfix(String postfix) throws Exception {
        Stack<Double> stack = new Stack<>();
        String[] tokens = postfix.trim().split("\\s+");
        for (String token : tokens) {
            char c = token.charAt(0);
            if (Character.isDigit(c) || (token.length() > 1 && (c == '-' || c == '.'))) {
                // Number
                stack.push(Double.parseDouble(token));
            } else if (isOperator(c)) {
                // Operator
                if (stack.size() < 2) throw new Exception("Invalid expression");
                double b = stack.pop();
                double a = stack.pop();
                switch (c) {
                    case '+':
                        stack.push(a + b);
                        break;
                    case '-':
                        stack.push(a - b);
                        break;
                    case '*':
                        stack.push(a * b);
                        break;
                    case '/':
                        if (b == 0) throw new Exception("Division by zero");
                        stack.push(a / b);
                        break;
                    case '^':
                        stack.push(Math.pow(a, b));
                        break;
                }
            } else if (isFunction(c)) {
                // Function
                if (stack.isEmpty()) throw new Exception("Invalid expression");
                double a = stack.pop();
                switch (c) {
                    case 's': // sin
                        stack.push(Math.sin(Math.toRadians(a)));
                        break;
                    case 'c': // cos
                        stack.push(Math.cos(Math.toRadians(a)));
                        break;
                    case 't': // tan
                        stack.push(Math.tan(Math.toRadians(a)));
                        break;
                    case 'g': // log
                        if (a <= 0) throw new Exception("Logarithm of non-positive number");
                        stack.push(Math.log10(a));
                        break;
                    case 'l': // ln
                        if (a <= 0) throw new Exception("Logarithm of non-positive number");
                        stack.push(Math.log(a));
                        break;
                    case 'q': // sqrt
                        if (a < 0) throw new Exception("Square root of negative number");
                        stack.push(Math.sqrt(a));
                        break;
                    case 'S': // asin
                        if (a < -1 || a > 1) throw new Exception("Invalid input for arcsin");
                        stack.push(Math.toDegrees(Math.asin(a)));
                        break;
                    case 'C': // acos
                        if (a < -1 || a > 1) throw new Exception("Invalid input for arccos");
                        stack.push(Math.toDegrees(Math.acos(a)));
                        break;
                    case 'T': // atan
                        stack.push(Math.toDegrees(Math.atan(a)));
                        break;
                    case 'G': // 10^x
                        stack.push(Math.pow(10, a));
                        break;
                    case 'E': // e^x
                        stack.push(Math.exp(a));
                        break;
                }
            } else {
                throw new Exception("Invalid token: " + token);
            }
        }
        if (stack.size() != 1) throw new Exception("Invalid expression");
        return stack.pop();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ScientificCalculator().setVisible(true);
        });
    }
}
