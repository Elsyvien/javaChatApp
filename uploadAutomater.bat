#!/usr/bin/env bash
#
# deploy_java_web.sh  — Quick one‑shot builder & deployer for a tiny Java servlet app
#
# Usage:
#   ./deploy_java_web.sh            # deploys default app 'MyWebApp'
#   ./deploy_java_web.sh ChatDemo   # deploys app called 'ChatDemo'
#
# The script *overwrites* any previous directory with the same APP_NAME,
# recompiles, re‑packages, and redeploys the .war into Tomcat10, then restarts Tomcat.
# Tested on Ubuntu 22.04 LTS with JDK 21 and tomcat10 package.
#
# -----------------------------------------------------------------------

set -euo pipefail

APP_NAME="${1:-MyWebApp}"
WAR_DIR="/var/lib/tomcat10/webapps"
SRC_DIR="${APP_NAME}/src"
WEBINF_DIR="${APP_NAME}/WEB-INF"
BUILD_DIR="${APP_NAME}/build"

echo "▶︎ Deploying Java web app: ${APP_NAME}"
echo "   (Any existing artefacts with that name will be replaced.)"

# 0. Ensure we can escalate for copy / restart
if ! sudo -n true 2>/dev/null; then
    echo "This script needs sudo to copy the WAR into Tomcat and restart it."
    echo "You may be asked for your password."
fi

# 1. Clean previous attempt (if any)
rm -rf "${APP_NAME}"

# 2. Create skeleton
mkdir -p "${SRC_DIR}" "${WEBINF_DIR}"

# 3. Generate HelloServlet.java
cat > "${SRC_DIR}/HelloServlet.java" <<EOF
import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class HelloServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = res.getWriter()) {
            out.println("<h1>Hello from ${APP_NAME}!</h1>");
            out.println("<p>Served via Java Servlet on $(date)</p>");
        }
    }
}
EOF

# 4. Generate web.xml
cat > "${WEBINF_DIR}/web.xml" <<EOF
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
         version="5.0">

  <servlet>
    <servlet-name>HelloServlet</servlet-name>
    <servlet-class>HelloServlet</servlet-class>
  </servlet>

  <servlet-mapping>
    <servlet-name>HelloServlet</servlet-name>
    <url-pattern>/hello</url-pattern>
  </servlet-mapping>

</web-app>
EOF

# 5. Compile
echo "▶︎ Compiling servlet..."
mkdir -p "${BUILD_DIR}/classes"
# Try to auto‑locate the servlet API JAR shipped with Tomcat
SERVLET_JAR=$(ls /usr/share/tomcat*/lib/jakarta.servlet-api*.jar 2>/dev/null | head -n 1)
if [[ -z "${SERVLET_JAR}" ]]; then
    SERVLET_JAR=$(ls /usr/share/tomcat*/lib/servlet-api.jar 2>/dev/null | head -n 1)
fi
if [[ -z "${SERVLET_JAR}" ]]; then
    echo "Could not locate servlet‑api jar. Adjust SERVLET_JAR in script."
    exit 1
fi

javac -d "${BUILD_DIR}/classes" -cp "${SERVLET_JAR}" ${SRC_DIR}/*.java

# 6. Assemble WAR structure
mkdir -p "${BUILD_DIR}/web/WEB-INF/classes"
cp -r "${BUILD_DIR}/classes"/* "${BUILD_DIR}/web/WEB-INF/classes/"
cp -r "${WEBINF_DIR}" "${BUILD_DIR}/web/"

# 7. Package WAR
echo "▶︎ Packaging WAR..."
(
  cd "${BUILD_DIR}/web"
  jar -cf "${APP_NAME}.war" .
)

# 8. Deploy to Tomcat
echo "▶︎ Deploying to Tomcat..."
sudo cp "${BUILD_DIR}/web/${APP_NAME}.war" "${WAR_DIR}/"
sudo systemctl restart tomcat10

echo "Done! Point your browser to: http://localhost:8080/${APP_NAME}/hello"
