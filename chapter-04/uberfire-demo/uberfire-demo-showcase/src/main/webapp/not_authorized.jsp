<%
  request.getSession().invalidate();
  String redirectURL = request.getContextPath()  +"/com.wordpress.marianbuenosayres.showcase.UberfireDemoShowcase/demo.html?message=Login failed: Not Authorized";
  response.sendRedirect(redirectURL);
%>
