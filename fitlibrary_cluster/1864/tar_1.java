package fitlibrary.flow;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;

import fit.Fixture;
import fitlibrary.annotation.ActionType;
import fitlibrary.annotation.AnAction;
import fitlibrary.annotation.ShowSelectedActions;
import fitlibrary.traverse.workflow.DoTraverse;
import fitlibrary.typed.TypedObject;

public class WhatIsInScope {
	public static String what(IScope scope, String substring) {
		StringBuilder s = new StringBuilder();
		s.append("<table>");
		for (TypedObject object: scope.objectsForLookup()) {
			Class<? extends Object> aClass = object.classType();
			ShowSelectedActions showAnnotation = aClass.getAnnotation(ShowSelectedActions.class);
			String className = aClass.getSimpleName();
			if (showAnnotation != null && !showAnnotation.rename().equals(""))
				className = showAnnotation.rename();
			s.append("<tr><td>"+className+"</td>\n");
			addActions(s,aClass,substring,showAnnotation != null);
			s.append("</tr>\n");
		}
		s.append("</table>");
		return s.toString();
	}

	private static void addActions(StringBuilder s, Class<? extends Object> aClass, String substring, boolean selective) {
		s.append("<td><table>");
		Method[] methods = aClass.getMethods();
		Arrays.sort(methods, new Comparator<Method>() {
			public int compare(Method m1, Method m2) {
				return m1.getName().compareToIgnoreCase(m2.getName());
			}
		});
		for (Method method : methods) {
			Class<?> declaringClass = method.getDeclaringClass();
			ShowSelectedActions showAnnotation = declaringClass.getAnnotation(ShowSelectedActions.class);
			boolean locallySelective = selective || showAnnotation != null;
			boolean ignoreDoTraverse = declaringClass == DoTraverse.class && aClass != DoTraverse.class;
			boolean matches = substring.isEmpty() || method.getName().contains(substring);
			if (matches &&
					!ignoreDoTraverse &&
					declaringClass != Object.class && 
					declaringClass != Fixture.class && 
					!method.getName().equals("getSystemUnderTest")) {
				AnAction action = method.getAnnotation(AnAction.class);
				if (action == null) {
					if (!locallySelective) {
						s.append("<tr><td>" + methodName(method) + "</td></tr>\n");
					}
				} else if (action.actionType() != ActionType.IGNORE) {
					String name = action.wiki();
					if (name.isEmpty())
						name = unCamel(method.getName(),action.actionType());
					if (action.actionType() == ActionType.PREFIX)
						name += "action...|";
					s.append("<tr><td><span class='note' title='"+action.tooltip()+"'>" + name + "</span></td>");
					if (action.actionType() == ActionType.COMPOUND && !method.getReturnType().isPrimitive()) {
						addActions(s, method.getReturnType(),substring,true);
					}
					s.append("</tr>");
				}
			}
		}
		s.append("</table></td>");
	}

	private static String methodName(Method method) {
		return unCamel(method.getName(),ActionType.SIMPLE) + parameters(method.getParameterTypes());
	}

	private static String parameters(Class<?>[] parameterTypes) {
		String s = "";
		for (Class<?> aClass : parameterTypes) {
			s += " ";
			s += aClass.getSimpleName();
			s += " |";
		}
		return s;
	}

  
  private static String unCamel(String name, ActionType actionType) {
	  String quotes = "''";
	  if (actionType == ActionType.PREFIX || actionType == ActionType.SUFFIX)
		  quotes = "'''";
	  String format = "i";
	  if (actionType == ActionType.PREFIX || actionType == ActionType.SUFFIX)
		  format = "b";
	  StringBuilder s = new StringBuilder();
	  s.append("|"+quotes+"<"+format+">");
	  for (char ch : name.toCharArray())
		  if (Character.isUpperCase(ch))
			  s.append(" "+Character.toLowerCase(ch));
		  else
			  s.append(""+ch);
	  s.append(quotes+"</"+format+">|");
	  return s.toString();
  }
}
