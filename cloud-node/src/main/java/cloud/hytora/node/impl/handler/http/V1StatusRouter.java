package cloud.hytora.node.impl.handler.http;

import cloud.hytora.common.DriverVersion;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.http.api.*;

import javax.annotation.Nonnull;


@HttpRouter("status")
public class V1StatusRouter {

	@HttpEndpoint(method = HttpMethod.GET)
	public void getIndex(@Nonnull HttpContext context) {
		CloudDriver<?> driver = CloudDriver.getInstance();

		context.getResponse()
			.setHeader("Content-Type", "application/json")
			.setBody(DocumentFactory.newJsonDocument(DriverVersion.getCurrentVersion()))
			.setStatusCode(HttpCodes.OK)
			.getContext()
			.closeAfter(true)
			.cancelNext(true);
	}

}
