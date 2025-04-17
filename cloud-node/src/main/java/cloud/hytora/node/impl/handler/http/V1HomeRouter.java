package cloud.hytora.node.impl.handler.http;


import cloud.hytora.driver.http.api.*;

import javax.annotation.Nonnull;


@HttpRouter("home")
public class V1HomeRouter {

	@HttpEndpoint(method = HttpMethod.GET)
	public void getIndex(@Nonnull HttpContext context) {
		context.getResponse()
			.setHeader("Content-Type", "application/json")
			.setBody("<!doctype html>\n" +
					"<html lang=\"de\">\n" +
					"<head>\n" +
					"    <meta charset=\"utf-8\">\n" +
					"    <title>Hytora Software</title>\n" +
					"    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
					"\n" +
					"    <meta property=\"theme-color\" content=\"#1e7eeb\">\n" +
					"    <meta property=\"og:title\" content=\"HytoraCloud\" />\n" +
					"    <meta property=\"og:description\" content=\"The home of yet another open-source minecraft server-management system (CloudSystem): HytoraCloud.\" />\n" +
					"    <meta property=\"og:url\" content=\"https://hytora.cloud/\" />\n" +
					"    <meta property=\"og:image\" content=\"https://hytora.cloud/img/logo-s.png\" />\n" +
					"\n" +
					"    <!-- Bootstrap core CSS -->\n" +
					"    <link href=\"css/bootstrap.min.css\" rel=\"stylesheet\">\n" +
					"    <script src=\"https://kit.fontawesome.com/f9ad965ec5.js\" crossorigin=\"anonymous\"></script>\n" +
					"\n" +
					"    <style>\n" +
					"        .bd-placeholder-img {\n" +
					"            font-size: 1.125rem;\n" +
					"            text-anchor: middle;\n" +
					"            -webkit-user-select: none;\n" +
					"            -moz-user-select: none;\n" +
					"            user-select: none;\n" +
					"        }\n" +
					"\n" +
					"        @media (min-width: 768px) {\n" +
					"            .bd-placeholder-img-lg {\n" +
					"                font-size: 3.5rem;\n" +
					"            }\n" +
					"        }\n" +
					"    </style>\n" +
					"\n" +
					"</head>\n" +
					"<body>\n" +
					"\n" +
					"<main>\n" +
					"\n" +
					"    <div class=\"container py-4\">\n" +
					"\n" +
					"        <nav class=\"navbar navbar-expand-lg navbar-dark bg-dark rounded-2\" style=\"margin-bottom: 25px;\">\n" +
					"            <div class=\"container-fluid\">\n" +
					"                <img height=\"22px\" src=\"img/logo-s.png\" style=\"margin-left: 10px; margin-right: 15px;\" width=\"40px;\">\n" +
					"                <span class=\"fs-5 navbar-brand\" style=\"margin-right: 10px;\">Hytora</span>\n" +
					"                <div class=\"collapse navbar-collapse\" id=\"navbarNav\">\n" +
					"                    <ul class=\"navbar-nav\">\n" +
					"                        <li class=\"nav-item\">\n" +
					"                            <a aria-current=\"page\" class=\"nav-link active\" href=\"#\">Home</a>\n" +
					"                        </li>\n" +
					"                        <li class=\"nav-item dropdown\">\n" +
					"                            <a aria-expanded=\"false\" class=\"nav-link dropdown-toggle\" data-bs-toggle=\"dropdown\" href=\"#\"\n" +
					"                               id=\"navbarDropdownMenuLink\" role=\"button\">\n" +
					"                                Software\n" +
					"                            </a>\n" +
					"                            <ul aria-labelledby=\"navbarDropdownMenuLink\" class=\"dropdown-menu\">\n" +
					"                                <li><a class=\"dropdown-item\" href=\"cloud.html\">Cloud</a></li>\n" +
					"                                <li><a class=\"dropdown-item disabled\" href=\"#\">Dash</a></li>\n" +
					"                            </ul>\n" +
					"                        </li>\n" +
					"                        <li class=\"nav-item\">\n" +
					"                            <a aria-current=\"page\" class=\"nav-link\" href=\"https://wiki.hytora.cloud\">Wiki</a>\n" +
					"                        </li>\n" +
					"                        <li class=\"nav-item\">\n" +
					"                            <a class=\"nav-link\" href=\"technology.html\">Technology</a>\n" +
					"                        </li>\n" +
					"                    </ul>\n" +
					"                </div>\n" +
					"                <form class=\"d-flex\">\n" +
					"                    <a class=\"link-info\"><i class=\"fab fa-twitter\" style=\"margin-right: 10px;\"></i></a>\n" +
					"                    <a href=\"https://discord.gg/5zCvJZHJPG\" class=\"link-info\"><i class=\"fab fa-discord\" style=\"margin-right: 10px;\"></i></a>\n" +
					"                    <a href=\"https://www.github.com/HytoraCloud/HytoraCloud\" class=\"link-info\"><i class=\"fab fa-github\"></i></a>\n" +
					"                </form>\n" +
					"            </div>\n" +
					"        </nav>\n" +
					"\n" +
					"        <div class=\"p-5 mb-4 bg-dark rounded-3\" style=\"color: white;\">\n" +
					"            <div class=\"container-fluid py-5\">\n" +
					"                <h1 class=\"display-5 fw-bold\">HytoraCloud</h1>\n" +
					"                <p class=\"col-md-8 fs-4\">A flexible, intuitive and easy-to-use cloud system made for your minecraft\n" +
					"                    server network!</p>\n" +
					"                <a href=\"cloud.html\"><button class=\"btn btn-info btn-lg\" type=\"button\">Learn more</button></a>\n" +
					"            </div>\n" +
					"        </div>\n" +
					"\n" +
					"        <div class=\"row align-items-md-stretch\">\n" +
					"            <div class=\"col-md-6\">\n" +
					"                <div class=\"h-100 p-5 bg-dark rounded-3\" style=\"color: white;\">\n" +
					"                    <h2>Technology behind our software</h2>\n" +
					"                    <p>Click the button below to learn more about how our software products work.</p>\n" +
					"                    <a href=\"technology.html\"><button class=\"btn btn-outline-info\" type=\"button\">Learn more</button></a>\n" +
					"                </div>\n" +
					"            </div>\n" +
					"            <div class=\"col-md-6\">\n" +
					"                <div class=\"h-100 p-5 bg-dark rounded-3\" style=\"color: white;\">\n" +
					"                    <h2>HytoraDash</h2>\n" +
					"                    <p>A self-hosted dashboard for our cloud-system.</p>\n" +
					"                    <button class=\"btn btn-outline-info disabled\" type=\"button\">Soon...</button>\n" +
					"                </div>\n" +
					"            </div>\n" +
					"        </div>\n" +
					"\n" +
					"        <footer class=\"mt-4 text-muted text-center footer mt-auto py-3\" style=\"color: white;\">\n" +
					"            <p>&copy; 2021 <a class=\"link-info\" href=\"https://hytora.cloud\">hytora.cloud</a> & <a class=\"link-info\" href=\"cxt.wtf\">cxt.wtf</a><br><a class=\"link-info\" href=\"imprint.html\">Imprint</a> <a class=\"link-info\"\n" +
					"                                                                                                   href=\"privacy.html\">Privacy\n" +
					"                Policy</a></p>\n" +
					"        </footer>\n" +
					"\n" +
					"        <script src=\"js/bootstrap.bundle.js\"></script>\n" +
					"    </div>\n" +
					"</main>\n" +
					"</body>\n" +
					"</html>\n")
			.setStatusCode(HttpCodes.OK)
			.getContext()
			.closeAfter(true)
			.cancelNext(true);
	}

}
