package com.necromantus.runtime;

import com.necromantus.lang.*;
import com.necromantus.parser.*;

import java.util.ArrayList;

/**
 * Language specific things here.
 * <p>
 * In Necromantus functions are not variables. They are treated as variables but they are not. Also
 * you cannot reassign a function. So you can be sure that function stays constant. So to make constant,
 * you just need to make a function that return constant value.
 *
 * @version 1.3
 */
public class Walker {

    public Scope scope;

    public Walker(Scope scope) {
        this.scope = scope;
    }

    private Object _walk(Node n) throws Exception {
        System.out.println("Walker: " + n);
        if (n.nodeType == NodeType.ACTION && n.nodeType2 == NodeType.ActionNodeType.OPERATOR) {

            switch (n.value.toString()) {
                case ("+"): {
                    return Float.parseFloat(_walk(n.left).toString()) + Float.parseFloat(_walk(n.right).toString());
                }
                case ("-"): {
                    return Float.parseFloat(_walk(n.left).toString()) - Float.parseFloat(_walk(n.right).toString());
                }
                case ("*"): {
                    return Float.parseFloat(_walk(n.left).toString()) * Float.parseFloat(_walk(n.right).toString());
                }
                case ("/"): {
                    return Float.parseFloat(_walk(n.left).toString()) / Float.parseFloat(_walk(n.right).toString());
                }
                default:
                    throw new Exception("Unknown operator " + n.value.toString() + " (" + n.value.toString() + ")");
            }
        } else if (n.nodeType == NodeType.VALUE && n.nodeType2 == NodeType.ValueNodeType.NUMBER) {
            return Float.parseFloat((String) n.value);
        } else if (n.nodeType == NodeType.VALUE && n.nodeType2 == NodeType.ValueNodeType.STRING) {
                return n.value.toString();
        } else if (n.nodeType == NodeType.EXPRESSION) {
            return _walk(n.left);
        } else if (n.nodeType == NodeType.VALUE && n.nodeType2 == NodeType.ValueNodeType.VAR) {
            if (scope.containsVar(n.value.toString())) {
                return scope.getVar(n.value.toString()).getValue();
            } else {
                throw new Exception("Unknown variable " + n.value + ".");
            }
        } else if (n.nodeType == NodeType.VALUE && n.nodeType2 == NodeType.ValueNodeType.FUNC_CALL_EXPR) {
            return _walk(n.left);
        } else if (n.nodeType == NodeType.STATEMENT && n.nodeType2 == NodeType.StatementNodeType.VAR_DEFINITION) {
            if (scope.containsVar(n.left.value.toString()) || scope.containsFunc(n.left.value.toString()))
                throw new Exception("Variable or function " + n.left.value.toString() + " already exists.");
            if (n.right.nodeType == NodeType.FUNC) {
                ArrayList<String> args = new ArrayList<>();
                for(Node node : ((ListNode) n.right.left).children) {
                    args.add(node.value.toString());
                }
                NMFunc f = new NMFunc((BlockNode) n.right.right, scope, args);
                f.name = n.left.value.toString();
                scope.funcs.add(f);
            }
            else scope.vars.add(new NMVariable(n.left.value.toString(), _walk(n.right)));
            return null;
        } else if (n.nodeType == NodeType.STATEMENT && n.nodeType2 == NodeType.StatementNodeType.VAR_ASSIGNMENT) {
            if (!scope.containsVar(n.left.value.toString()))
                throw new Exception("Variable " + n.left.value.toString() + " doesn't exist.");
            scope.setVar(n.left.value.toString(), _walk(n.right));
            return null;
        } else if (n.nodeType == NodeType.ACTION && n.nodeType2 == NodeType.ActionNodeType.FUNC_CALL) {
            if (!scope.containsFunc(n.left.value.toString()))
                throw new Exception("Function " + n.left.value.toString() + " does not exist");
            ArrayList<Object> args = new ArrayList<>();
            for (Node node : ((ListNode) n.right).children) {
                args.add(_walk(node));
            }
            return scope.getFunc(n.left.value.toString()).call(args);
        }
        throw new Exception("Uh... Something went wrong... That's not your fault! DEBUG: Walker doesn't know this thing, this feature will be removed or walker" +
                "will be updated. nodeType = "
                + n.nodeType + " nodeType2 = " + n.nodeType2 + "  value = " + n.value);
    }

    public Object walk(Node node) throws Exception {
        return _walk(node);
    }

    public void put(String name, Object val) {
        scope.vars.add(new NMVariable(name, val));
    }

    public NMVariable get(String name) {
        return scope.getVar(name);
    }

    public NMFunction getFunc(String name) {
        return scope.getFunc(name);
    }

    public void putNativeFunc(NMNativeFunc func) {
        scope.funcs.add(func);
    }
}
