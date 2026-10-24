//高德地图 JS API 动态加载工具
//⚠️ 安全密钥必须在脚本加载前配置（JS API 2.0 强制要求）
//使用控制台里"健康颐养平台 前端定位Key"（Web端 类型），安全密钥与之配对
window._AMapSecurityConfig = {
    securityJsCode: 'b718269c0c5a625de3e81287471abedc',
}

//加载高德地图，返回 AMap 全局对象（已加载过则直接返回）
export const loadAMap = () => {
    return new Promise((resolve, reject) => {
        if (window.AMap) {
            resolve(window.AMap)
            return
        }
        const script = document.createElement('script')
        script.src = 'https://webapi.amap.com/maps?v=2.0&key=bddca8a364e66a8e57184a637df55c99' +
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
