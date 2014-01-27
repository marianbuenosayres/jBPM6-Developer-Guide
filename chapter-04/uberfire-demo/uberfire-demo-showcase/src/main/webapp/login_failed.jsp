<%
  request.getSession().invalidate();
  String redirectURL = request.getContextPath()  +"/com.wordpress.marianbuenosayres.showcase.UberfireDemoShowcase/demo.html?message=Login failed: Invalid UserName or Password";
  response.sendRedirect(redirectURL);
%>
