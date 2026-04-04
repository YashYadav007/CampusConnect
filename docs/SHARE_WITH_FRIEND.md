# Share With a Friend

Use temporary tunnel URLs instead of deploying.

## 1. Start the backend

```bash
cd /Users/yashyadav/Documents/CampusConnect
./mvnw spring-boot:run
```

## 2. Start the frontend

```bash
cd /Users/yashyadav/Documents/CampusConnect/frontend
npm run dev
```

Local development keeps working with the Vite proxy when `VITE_API_BASE_URL` is not set.

## 3. Start a backend tunnel

```bash
cloudflared tunnel --url http://localhost:8080
```

Copy the public backend URL, for example:

```text
https://your-backend.trycloudflare.com
```

## 4. Point the frontend to the backend tunnel

Create or update `frontend/.env.local`:

```bash
cd /Users/yashyadav/Documents/CampusConnect/frontend
cat > .env.local <<'EOF'
VITE_API_BASE_URL=https://your-backend.trycloudflare.com
EOF
```

Restart the frontend after changing the env file:

```bash
npm run dev
```

## 5. Allow the tunneled frontend origin in backend CORS

Set `APP_CORS_ALLOWED_ORIGINS` before starting the backend, including:

- `http://localhost:5173`
- `http://127.0.0.1:5173`
- your frontend tunnel origin

Example:

```bash
export APP_CORS_ALLOWED_ORIGINS="http://localhost:5173,http://127.0.0.1:5173,https://your-frontend.trycloudflare.com"
./mvnw spring-boot:run
```

If you start the backend before you know the frontend tunnel URL, restart the backend after setting this variable.

## 6. Start a frontend tunnel

```bash
cloudflared tunnel --url http://localhost:5173
```

Copy the public frontend URL, for example:

```text
https://your-frontend.trycloudflare.com
```

## 7. Share the frontend URL

Send your friend the frontend tunnel URL:

```text
https://your-frontend.trycloudflare.com
```

They will use the tunneled frontend, which will call the tunneled backend.
