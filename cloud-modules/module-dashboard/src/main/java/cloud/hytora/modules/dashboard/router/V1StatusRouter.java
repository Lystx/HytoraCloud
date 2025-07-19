package cloud.hytora.modules.dashboard.router;

import cloud.hytora.common.VersionInfo;
import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.http.api.*;

import javax.annotation.Nonnull;


@HttpRouter("status")
public class V1StatusRouter {

	@HttpEndpoint(method = HttpMethod.GET)
	public void getIndex(@Nonnull HttpContext context) {
		CloudDriver driver = CloudDriver.getInstance();

		context.getResponse()
			.setHeader("Content-Type", "application/json")
			.setBody(Document.gson(VersionInfo.getCurrentVersion()))
			.setStatusCode(HttpCodes.OK)
			.getContext()
			.closeAfter(true)
			.cancelNext(true);
	}

}
