package cloud.hytora.modules.dashboard.router;


import cloud.hytora.document.Document;
import cloud.hytora.driver.common.http.api.*;

import javax.annotation.Nonnull;


@HttpRouter("ping")
public class V1PingRouter {

	@HttpEndpoint(method = HttpMethod.GET)
	public void getIndex(@Nonnull HttpContext context) {
		context.getResponse()
			.setHeader("Content-Type", "application/json")
			.setBody(Document.gson("success", true))
			.setStatusCode(HttpCodes.OK)
			.getContext()
			.closeAfter(true)
			.cancelNext(true);
	}

}
