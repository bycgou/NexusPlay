import request from '@/utils/request'

// 登录接口
export const login = (data: { email: string; password: string }) => {
    return request({
        url: '/pp/people/login',
        method: 'post',
        data
    })
};

// 获取图片验证码
export const getImageCaptcha = () => {
    return request({
        url: '/pp/people/captcha/image',
        method: 'get'
    });
};

// 发送邮箱验证码
export const sendEmailCode = (data: { email: string; captchaId: string; imageCaptcha: string }) => {
    return request({
        url: '/pp/people/email',
        method: 'post',
        data
    });
};

// 注册
export const register = (data: { email: string; password: string; emailCaptcha: string }) => {
    return request({
        url: '/pp/people/register',
        method: 'post',
        data
    });
};

// 获取用户信息接口
export const getUserInfo = (userId: number) => {
    return request({
        url: `/pp/people/user/${userId}`,
        method: 'get'
    });
};

// 更改用户信息接口
export const updateUserInfo = (data: any) => {
    return request({
        url: '/pp/people/updateUserInfo',
        method: 'post',
        data
    })
};

// 修改密码
export const changePassword = (data: { oldPassword: string; newPassword: string }) => {
    return request({
        url: '/pp/people/changePassword',
        method: 'post',
        data
    })
};

// 搜索用户（昵称/用户名）
export const searchUsers = (keyword: string, limit = 20) => {
    return request({
        url: '/pp/people/search',
        method: 'get',
        params: { keyword, limit }
    })
};

export {request}