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
        "tan"
    };

    private JButton[] buttons = new JButton[buttonLabels.length];
    private StringBuilder input = new StringBuilder();

    public ScientificCalculator() {
        setTitle("Scientific Calculator");
        setSize(500, 600); // Increased size to accommodate larger buttons
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create display field
        display = new JTextField();
        display.setEditable(false);
        display.setFont(new Font("Arial", Font.BOLD, 24));
        display.setHorizontalAlignment(JTextField.RIGHT); // Align text to the right
        add(display, BorderLayout.NORTH);

        // Create panel for buttons
        panel = new JPanel();
        panel.setLayout(new GridLayout(6, 5, 5, 5)); // 6 rows, 5 columns

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
        } else if (command.equals("=")) {
            try {
                double result = evaluateExpression(input.toString());
                display.setText(Double.toString(result));
                input.setLength(0); // Clear input after evaluation
            } catch (Exception ex) {
                display.setText("Error");
                input.setLength(0);
            }
        } else {
            input.append(command);
            display.setText(input.toString());
        }
    }

    // Evaluate the mathematical expression
    private double evaluateExpression(String expression) throws Exception {
        // Replace functions with single-letter tokens
        expression = expression.replaceAll("sin", "s");
        expression = expression.replaceAll("cos", "c");
        expression = expression.replaceAll("tan", "t");
        expression = expression.replaceAll("log", "g");
        expression = expression.replaceAll("ln", "l");
        expression = expression.replaceAll("sqrt", "q");

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
        return c == 's' || c == 'c' || c == 't' || c == 'g' || c == 'l' || c == 'q';
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '^';
    }

    private int precedence(char op) {
        switch (op) {
            case '^':
                return 3;
            case '*':
            case '/':
                return 2;
            case '+':
            case '-':
                return 1;
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
                    case 's':
                        stack.push(Math.sin(Math.toRadians(a)));
                        break;
                    case 'c':
                        stack.push(Math.cos(Math.toRadians(a)));
                        break;
                    case 't':
                        stack.push(Math.tan(Math.toRadians(a)));
                        break;
                    case 'g':
                        if (a <= 0) throw new Exception("Logarithm of non-positive number");
                        stack.push(Math.log10(a));
                        break;
                    case 'l':
                        if (a <= 0) throw new Exception("Logarithm of non-positive number");
                        stack.push(Math.log(a));
                        break;
                    case 'q':
                        if (a < 0) throw new Exception("Square root of negative number");
                        stack.push(Math.sqrt(a));
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
