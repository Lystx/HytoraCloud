package cloud.hytora.modules.dashboard.router;


import cloud.hytora.common.collection.pair.Tuple;
import cloud.hytora.document.Document;
import cloud.hytora.driver.common.http.api.*;

import javax.annotation.Nonnull;
import java.util.List;


@HttpRouter("upgrade")
public class V1UpgradeRouter {

	@HttpEndpoint(method = HttpMethod.GET)
	public void upgrade(@Nonnull HttpContext context) {

		// TODO this auth code is mostly copied
		Tuple<HttpAuthHandler, HttpAuthUser> values = Tuple.empty();
		List<String> auth = context.getRequest().getQueryParameters().get("auth");
		if (auth == null || auth.isEmpty()) {
			context.getResponse().setStatusCode(HttpCodes.UNAUTHORIZED);
			return;
		}
		context.getServer().applyUserAuth(values, auth.get(0));
		if (values.getSecond() == null) {
			context.getResponse().setStatusCode(HttpCodes.UNAUTHORIZED);
			return;
		}
		if (!values.getSecond().hasPermission("web.upgrade")) {
			context.getResponse().setStatusCode(HttpCodes.FORBIDDEN).setBody("Permission " + "web.upgrade" + " required");
			return;
		}

		WebSocketChannel websocket = context.upgrade();
		websocket.sendFrame(WebSocketFrameType.TEXT, "hi");
	}

}
