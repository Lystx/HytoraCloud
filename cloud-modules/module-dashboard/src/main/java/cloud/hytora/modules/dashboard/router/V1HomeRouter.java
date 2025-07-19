package cloud.hytora.modules.dashboard.router;


import cloud.hytora.driver.common.http.api.*;

import javax.annotation.Nonnull;


@HttpRouter("home")
public class V1HomeRouter {

	@HttpEndpoint(method = HttpMethod.GET)
	public void getIndex(@Nonnull HttpContext context) {
		context.getResponse()
			.setContentType(HttpContent.HTML_TEXT)
			.setHeader("Accept", "application/json")
			.setBody("<form action=\"/foo\" method=\"post\" enctype=\"multipart/form-data\">\n" +
					"  <input type=\"text\" name=\"description\" value=\"Description input value\" />\n" +
					"  <input type=\"file\" name=\"myFile\" />\n" +
					"  <button type=\"submit\">Submit</button>\n" +
					"</form>")
			.setStatusCode(HttpCodes.OK)
			.getContext()
			.closeAfter(true)
			.cancelNext(true);
	}

}
