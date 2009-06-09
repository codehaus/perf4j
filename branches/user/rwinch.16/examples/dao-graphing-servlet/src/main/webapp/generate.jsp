<html>
<body>
	<h1>You slept for <%= request.getAttribute("sleep") %> ms for tag <% request.getAttribute("tag"); %></h1>
	
	<form action="generate">
		<p>
			<label for="tag">Tag</label>
			<input id="tag" type="text"	name="tag" value="<%= request.getAttribute("tag") %>" />
		</p>
		<p>
			<label for="sleep">Sleep</label>
			<input type="text" name="sleep" value="<%= request.getAttribute("sleep") %>"/>
		</p>
		<p>
			<input type="submit" value="Submit"/>
		</p>
	</form>
	<ul>
		<li><a href="generate">Create Statistics</a></li>
		<li><a href="perf4j">View Statistics</a></li>
	</ul>
</body>
</html>
