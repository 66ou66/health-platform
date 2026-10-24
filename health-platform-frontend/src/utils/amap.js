//高德地图 JS API 动态加载工具
//⚠️ Key 与安全密钥通过 Vite 环境变量注入，不入库：
//   在本目录（health-platform-frontend/）创建 .env.local（可参考 .env.example）：
//     VITE_AMAP_KEY=Web端(JS API) 类型 Key
//     VITE_AMAP_SECURITY_CODE=与该 Key 配对的安全密钥
//   说明：这两个值会被打包进前端产物、对页面访问者可见，安全防线是高德控制台的域名白名单。
const AMAP_KEY = import.meta.env.VITE_AMAP_KEY || ''
const AMAP_SECURITY_CODE = import.meta.env.VITE_AMAP_SECURITY_CODE || ''

//安全密钥必须在脚本加载前配置（JS API 2.0 强制要求）
if (AMAP_SECURITY_CODE) {
    window._AMapSecurityConfig = {
        securityJsCode: AMAP_SECURITY_CODE,
    }
}

//加载高德地图，返回 AMap 全局对象（已加载过则直接返回）
export const loadAMap = () => {
    return new Promise((resolve, reject) => {
        if (!AMAP_KEY) {
            reject(new Error('未配置高德地图 Key：请参考 .env.example 创建 .env.local 并填入 VITE_AMAP_KEY'))
            return
        }
        if (window.AMap) {
            resolve(window.AMap)
            return
        }
        const script = document.createElement('script')
        script.src = 'https://webapi.amap.com/maps?v=2.0&key=' + AMAP_KEY +
            '&plugin=AMap.Geolocation,AMap.GeometryUtil'
        script.onload = () => {
            //脚本加载成功但 AMap 未定义 = SDK 初始化被拒（白名单/安全密钥问题）
            if (!window.AMap) {
                reject(new Error('SDK初始化失败，请检查该Key的域名白名单(127.0.0.1)与安全密钥'))
                return
            }
            resolve(window.AMap)
        }
        script.onerror = () => reject(new Error('高德地图加载失败，请检查Key与域名白名单'))
        document.head.appendChild(script)
    })
}

//获取当前定位（高德定位，精度高于浏览器定位），失败返回 null
export const getMyLocation = (AMap) => {
    return new Promise((resolve) => {
        const geolocation = new AMap.Geolocation({ enableHighAccuracy: true, timeout: 8000 })
        geolocation.getCurrentPosition((status, result) => {
            if (status === 'complete' && result.position) {
                resolve({ longitude: result.position.lng, latitude: result.position.lat })
            } else {
                resolve(null)
            }
        })
    })
}

//两点距离（米）
export const distanceMeters = (AMap, p1, p2) => {
    return AMap.GeometryUtil.distance(p1, p2)
}
