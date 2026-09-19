# Contributing — local run

1. Clone the repo.
2. Install JDK 17 and Maven 3.9+.
3. `mvn -B clean verify`
4. Deploy `target/sammart.war` to Tomcat 9 `webapps/`.
5. Open `http://localhost:8080/sammart/`.

Do not commit `config.properties` with production passwords. Use `.env.example` as the checklist of keys.

Branch model: `feature/<name>` → PR into `main`. Conventional commits: `feat:`, `fix:`, `test:`, `docs:`.
