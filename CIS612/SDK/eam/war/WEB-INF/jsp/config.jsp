<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@page import="com.vmware.eam.sample.solution.ui.ConfigModel" %>
<%@page import="com.vmware.eam.sample.solution.util.Pair" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
       <meta http-equiv="content-type" content="text/html; charset=UTF-8">
       <title>EAM Sample Solution Configuration</title>
       <link rel="stylesheet" href="eam.css" type="text/css"/>
       <script language="Javascript" type="text/javascript">
          function checkstate(str, name) {
             document.forms[1].goalState.value = str;
             return window.confirm('Are you sure you want to perform the action ' + name + '?');
          }
       </script>
    </head>
    <body>
       <%
          ConfigModel model = (ConfigModel) request.getAttribute("model");
          if (model.getMessage() != null) {
             out.println("<div class=\"notification\">" + model.getMessage()
                   + "</div>");
          }
       %>

       <table width="450px" cellpadding="0" cellspacing="0">
          <tr><td valign="top">
          <table width="175px" cellpadding="0" cellspacing="0" class="Box">
             <tr class="Box-Header"><td>Solution configuration</td></tr>
             <tr><td>
                <form action="${model.configUrl}" method="post">
                <fieldset>
                   <legend>Compute resources</legend>
                   <select style="width:160px" name="scope" size="5" multiple>
                      <%
                         if (!model.isAgentHandlerUnregistered()) {
                            for (Pair<String, Boolean> cr : model.getComputeResources()) {
                               if (cr.getSecond().booleanValue()) {
                                  out.println("<option value=\"" + cr.getFirst() + "\" selected>"
                                              + cr.getFirst() + "</option>");
                               } else {
                                  out.println("<option value=\"" + cr.getFirst() + "\">"
                                              + cr.getFirst() + "</option>");
                               }
                            }
                         }
                      %>
                   </select>
                </fieldset>
                <fieldset>
                   <center>
                   <%
                      if (model.isAgentHandlerUnregistered()) {
                         out.println("<input type=\"submit\" value=\"Update Configuration\" disabled />");
                      } else {
                         out.println("<input type=\"submit\" value=\"Update Configuration\" />");
                      }
                   %>
                   </center>
                </fieldset>
                </form>
             </td></tr>
          </table>
          </td><td valign="top">
          <table width="175px" cellpadding="0" cellspacing="0" class="Box">
             <tr class="Box-Header"><td>Solution Actions</td></tr>
             <tr><td>
                <form action="${model.configUrl}" method="post">
                   <input type="hidden" name="goalState">
                   <fieldset>
                   <legend>Actions</legend>
                   <center>
                      <%
                         for (String state : model.getStates()) {
                            String goalState = model.getGoalState(state);
                            out.println("<input style=\"width:150px;margin-bottom:2px\""
                                  + " onclick=\"return checkstate('" + goalState + "','"
                                  + state + "');\" type=\"submit\" value=\"" + state + "\"");
                            if (model.isAgentHandlerUnregistered()
                                  || model.getGoalState().equals(goalState)) {
                               out.println("disabled");
                            }
                            out.println("/><br/>");
                         }
                      %>
                   </center>
                   </fieldset>
                </form>
             </td></tr>
          </table>
          </td></tr>
       </table>
    </body>
</html>
