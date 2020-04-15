package fitlibrary.flow;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;

import fitlibrary.annotation.ActionType;
import fitlibrary.annotation.AnAction;
import fitlibrary.annotation.ShowSelectedActions;
import fitlibrary.typed.TypedObject;

public class WhatIsInScope {
	public static String what(IScope scope) {
		StringBuilder s = new StringBuilder();
		s.append("<table>");
		for (TypedObject object: scope.objectsForLookup()) {
			Class<? extends Object> aClass = object.classType();
			ShowSelectedActions showAnnotation = aClass.getAnnotation(ShowSelectedActions.class);
			String className = aClass.getSimpleName();
			if (showAnnotation != null && !showAnnotation.rename().equals(""))
				className = showAnnotation.rename();
			s.append("<tr><td>"+className+"</td>\n");
			addActions(s,aClass,showAnnotation != null);
			s.append("</tr>\n");
		}
		s.append("</table>");
		return s.toString();
	}

  private static void addActions(StringBuilder s, Class<? extends Object> aClass, boolean selective) {
	  s.append("<td><table>");
	  Method[] methods = aClass.getMethods();
	  Arrays.sort(methods, new Comparator<Method>() {
		  public int compare(Method m1, Method m2) {
			  return m1.getName().compareToIgnoreCase(m2.getName());
		  }
	  });
	  for (Method method : methods) {
		  if (method.getDeclaringClass() != Object.class && !method.getName().equals("getSystemUnderTest")) {
			  AnAction action = method.getAnnotation(AnAction.class);
			  if (action == null) {
				  if (!selective)
					  s.append("<tr><td>" + method.getName() + "/"
							  + method.getParameterTypes().length + "</td></tr>\n");
			  } else if (action.actionType() != ActionType.IGNORE) {
				  String name = action.wiki();
				  if (name.isEmpty())
					  name = unCamel(method.getName(),action.actionType());
				  if (action.actionType() == ActionType.PREFIX)
					  name += "action...|";
				  s.append("<tr><td><span class='note' title='"+action.tooltip()+"'>" + name + "</span></td>");
				  if (action.actionType() == ActionType.COMPOUND && !method.getReturnType().isPrimitive()) {
					  addActions(s, method.getReturnType(),true);
				  }
				  s.append("</tr>");
			  }
		  }
	  }
	  s.append("</table></td>");
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
