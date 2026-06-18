$bt = [byte]96
$dl = [byte]36
$sq = [byte]39
$lines = @(
"import axios from \"" + "/node_modules/.vite/deps/axios.js?v=e6238431" + "\"",
"import { API_BASE } from \"" + "/src/api/env.js" + "\"",
"",
"const http = axios.create({",
"  baseURL: API_BASE,",
"  timeout: 30000,",
"})"
)
# ... more content
Write-Host "Script created"
