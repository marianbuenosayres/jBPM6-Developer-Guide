<%
    String queryString = request.getQueryString();
    String redirectURL = request.getContextPath()  +"/com.wordpress.marianbuenosayres.showcase.UberfireDemoShowcase/demo.html?"+(queryString==null?"":queryString);
    response.sendRedirect(redirectURL);
%>
