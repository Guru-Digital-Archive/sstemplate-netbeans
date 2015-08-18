<html>
    <body>
        <img src="$Image.URL" alt="$Image.Title"/>
        <ul>
            <%-- usage example: loop --%>
            <% loop Menu %>
			<li>$Title</li>
            <% end_loop %>
        </ul>
    </body>
</html>
