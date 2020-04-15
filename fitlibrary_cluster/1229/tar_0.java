package fitlibrary.flow;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import fit.Fixture;
import fitlibrary.annotation.ActionType;
import fitlibrary.annotation.AnAction;
import fitlibrary.annotation.CompoundAction;
import fitlibrary.annotation.NullaryAction;
import fitlibrary.annotation.ShowSelectedActions;
import fitlibrary.annotation.SimpleAction;
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
		Method[] methods = aClass.getMethods();
		Arrays.sort(methods, new Comparator<Method>() {
			public int compare(Method m1, Method m2) {
				return m1.getName().compareToIgnoreCase(m2.getName());
			}
		});
		boolean first = true;
		for (Method method : methods) {
			if (!ignoreMethod(aClass,method)) {
				boolean locallySelective = selective || method.getDeclaringClass().getAnnotation(ShowSelectedActions.class) != null;
				ActionInfo actionInfo = decodeAnnotation(method,locallySelective);
				if (!actionInfo.ignore && (matchAll || actionInfo.matches(pattern))) {
					if (first)
						s.append("<tr><td><h4>Action</h4></td>"+
								"<td><h4><span title='The Java return type.'>Returns</span></h4></td>"+
								"<td><h4><span title='Actions that can occur in the rest of the table.'>Following actions</span></h4></td></tr>\n");
					first = false;					
					actionInfo.display(s,returnTypeDisplay(method));
					Class<?> returnType = method.getReturnType();
					if (actionInfo.compound && !ignoreType(returnType))
						addActions(s,returnType,"",true);
				}
			}
		}
		s.append("</table></td>");
	}
	
	private static boolean ignoreMethod(Class<? extends Object> aClass, Method method) {
		Class<?> declaringClass = method.getDeclaringClass();
		return declaringClass == Object.class || 
		       declaringClass == Fixture.class ||
		       (declaringClass == DoTraverse.class && aClass != DoTraverse.class) ||
		       method.getName().equals("getSystemUnderTest");
	}
	
	static class ActionInfo {
		public final String name;
		public final String tooltip;
		public final boolean ignore;
		public final boolean compound;

		public ActionInfo(String name, String tooltip, boolean compound, boolean hasParameters) {
			this.name = name;
			if (tooltip.isEmpty())
				if (hasParameters)
					this.tooltip = "Action in sequence form, where the method name is followed by the types of each of the arguments.\n\n"+
					               "This has been determined automatically from the underlying method.\n\n"+
					               "If you want better documentation, which shows how to mix keywords and arguments, ask the developer who wrote the fixturing code to provide it. "+
					               "(See .FitLibrary.FitLibrary.SpecifiCations.GlobalActionsProvided.WhatIsInScope for how to do this.)";
				else
					this.tooltip = "Action name has been determined automatically from the name of the underlying method.";
			else
				this.tooltip = tooltip.replace("\"", "'");
			this.compound = compound;
			this.ignore = false;
		}
		public ActionInfo() {
			this.name = "";
			this.tooltip = "";
			this.compound = false;
			this.ignore = true;
		}
		public boolean matches(String pattern) {
			return tooltip.contains(pattern) || nameWithoutTags().contains(pattern);
		}
		public String nameWithoutTags() {
			return name.replaceAll("<i>","").replaceAll("</i>","").replaceAll("<b>","").replaceAll("</b>","");
		}
		public void display(StringBuilder s, String returns) {
			s.append("<tr><td><span style='background-color: #ffffcc' title=\""+tooltip+"\">" + name + "</span></td><td>"+returns+"</td>");
		}
		public static ActionInfo ignore() {
			return new ActionInfo();
		}
	}
	  
	private static String returnTypeDisplay(Method method) {
		Class<?> returnType = method.getReturnType();
		if (returnType == Void.TYPE || returnType == TwoStageSpecial.class)
			return "";
		return returnType.getSimpleName();
	}
	
	private static ActionInfo decodeAnnotation(Method method, boolean selective) {
		boolean hasParameters = method.getParameterTypes().length > 0;
		NullaryAction nullaryAction = method.getAnnotation(NullaryAction.class);
		if (nullaryAction != null)
			return new ActionInfo(unCamel(method.getName(),ActionType.SIMPLE),nullaryAction.tooltip(),false,hasParameters);
		SimpleAction simpleAction = method.getAnnotation(SimpleAction.class);
		if (simpleAction != null)
			return new ActionInfo(simpleAction.wiki(),simpleAction.tooltip(),false,hasParameters);
		CompoundAction compoundAction = method.getAnnotation(CompoundAction.class);
		if (compoundAction != null) {
			String name = compoundAction.wiki();
			if (name.isEmpty())
				name = unCamel(method.getName(),ActionType.SIMPLE);
			return new ActionInfo(name,compoundAction.tooltip(),true,hasParameters);
		}
		AnAction anAction = method.getAnnotation(AnAction.class);
		if (anAction != null) {
			if (anAction.actionType() == ActionType.IGNORE)
				return ActionInfo.ignore();
			String name = anAction.wiki();
			if (name.isEmpty())
				name = unCamel(method.getName(),anAction.actionType());
			if (anAction.actionType() == ActionType.PREFIX)
				name += "action...|";
			return new ActionInfo(name,anAction.tooltip(),anAction.isCompound(),hasParameters);
		}
		if (selective)
			return ActionInfo.ignore();
		return new ActionInfo(methodName(method),"",false,hasParameters);
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
