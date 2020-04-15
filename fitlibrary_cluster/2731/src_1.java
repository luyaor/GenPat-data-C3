package fitlibrary.flow;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import fit.Fixture;
import fitlibrary.annotation.ActionType;
import fitlibrary.annotation.AnAction;
import fitlibrary.annotation.ShowSelectedActions;
import fitlibrary.traverse.workflow.DoTraverse;
import fitlibrary.traverse.workflow.caller.TwoStageSpecial;
import fitlibrary.typed.TypedObject;

public class WhatIsInScope {
	public static String what(IScope scope, String initialPattern) {
		String pattern = initialPattern.replace("<","&lt;");
		StringBuilder s = new StringBuilder();
		s.append("<table>");
		s.append("<tr><td><h4>Object of type</h4></td><td><h4>Action Details</h4></td></tr>\n");
		for (TypedObject object: scope.objectsForLookup()) {
			Class<? extends Object> aClass = object.classType();
			ShowSelectedActions showAnnotation = aClass.getAnnotation(ShowSelectedActions.class);
			s.append("<tr><td>"+aClass.getSimpleName()+"</td>\n");
			addActions(s,aClass,pattern,showAnnotation != null);
			s.append("</tr>\n");
		}
		s.append("</table>");
		return s.toString();
	}

	private static void addActions(StringBuilder s, Class<? extends Object> aClass, String pattern, boolean selective) {
		boolean matchAll = pattern.isEmpty();
		s.append("<td><table>");
		s.append("<tr><td><h4>Action</h4></td><td><h4>Return type</h4></td><td><h4>Following actions</h4></td></tr>\n");
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
			if (	!ignoreDoTraverse &&
					declaringClass != Object.class && 
					declaringClass != Fixture.class && 
					!method.getName().equals("getSystemUnderTest")) {
				Class<?> returnType = method.getReturnType();
				String returns = returnType.getSimpleName();
				if (returnType == Void.TYPE || returnType == TwoStageSpecial.class)
					returns = "";
				AnAction action = method.getAnnotation(AnAction.class);
				if (action == null) {
					String methodName = methodName(method);
					String nameWithoutTags = methodName.replaceAll("<i>","").replaceAll("</i>","").replaceAll("<b>","").replaceAll("</b>","");
					boolean matches = matchAll || nameWithoutTags.contains(pattern);
					if (!locallySelective && matches) {
						s.append("<tr><td>" + methodName + "</td><td>"+returns+"</td></tr>\n");
					}
				} else if (action.actionType() != ActionType.IGNORE) {
					String name = action.wiki();
					if (name.isEmpty())
						name = unCamel(method.getName(),action.actionType());
					String tooltip = action.tooltip();
					String nameWithoutTags = name.replaceAll("<i>","").replaceAll("</i>","").replaceAll("<b>","").replaceAll("</b>","");
					boolean matches = matchAll || nameWithoutTags.contains(pattern) || tooltip.contains(pattern);
					if (matches) {
						if (action.actionType() == ActionType.PREFIX)
							name += "action...|";
						s.append("<tr><td><span class='note' title='"+tooltip+"'>" + name + "</span></td><td>"+returns+"</td>");
						if (action.actionType() == ActionType.COMPOUND && !ignoreType(returnType)) {
							addActions(s,returnType,"",true);
						}
						s.append("</tr>");
					}
				}
			}
		}
		s.append("</table></td>");
	}

	private static boolean ignoreType(Class<?> type) {
		return type.isPrimitive() || type.isEnum() || type.isArray() ||
		       type == String.class || type == Date.class ||
		       Number.class.isAssignableFrom(type) ||
		       type == Character.class;
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
