
import axios from 'axios';
import {ElMessage} from 'element-plus';

export interface uploadOptions{
    /*
    最大文件大小(字节) ,默认5MB
     */
    maxSize?:number;
    /** 允许的 MIME 类型，默认 image/* */
    accept?: string;
    /** 上传接口地址 */
    uploadUrl: string;
    /** 上传字段名，默认 'file' */
    fieldName?: string;
    /** 是否显示成功/失败提示 */
    showNotification?: boolean;
}

export const uploadImage = async (
    file:File,
    options:uploadOptions
):Promise<string> => {
    const{
        maxSize = 5 * 1024 * 1024,
        accept = 'image/*',
        uploadUrl,
        fieldName = 'file',
        showNotification = true
    } = options;

    // 校验文件类型
    if(!accept.startsWith('image/')&&!file.type.startsWith('image/')){
        const msg = '请上传图片';
        if(showNotification){
            ElMessage.error(msg);
        }throw new Error(msg);
    }
    // 校验文件大小
    if(file.size>maxSize){
        const maxSizeMB = maxSize / 1024 / 1024;
        const msg = `文件大小不能超过 ${maxSizeMB} MB`;
        if(showNotification){
            ElMessage.error(msg);
        }throw new Error(msg);
    }

    const formData = new FormData();
    formData.append(fieldName, file);

    try {
        const token = localStorage.getItem('token');
        const res = await axios.post(uploadUrl, formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
                ...(token ? { Authorization: `Bearer ${token}` } : {})
            }
        });
        // 适配你的后端返回格式：res.data.code === 1 或 200 表示成功
        if (res.data?.code === 200 || res.data?.code === 1) {
            const url = res.data.data;
            if (!url) {
                throw new Error('上传成功但未返回图片地址');
            }
            if (showNotification) ElMessage.success('上传成功');
            return url;
        } else {
            const msg = res.data?.msg || '上传失败，请重试';
            if (showNotification) ElMessage.error(msg);
            throw new Error(msg);
        }
    } catch (err: any) {
        const msg = err.message || '网络错误，请重试';
        if (showNotification) ElMessage.error(msg);
        throw err;
    }
};


