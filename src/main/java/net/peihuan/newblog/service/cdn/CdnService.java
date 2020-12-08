package net.peihuan.newblog.service.cdn;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.cdn.model.v20141111.RefreshObjectCachesRequest;
import com.aliyuncs.cdn.model.v20141111.RefreshObjectCachesResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.profile.DefaultProfile;
import lombok.extern.slf4j.Slf4j;
import net.peihuan.newblog.bean.enums.ResultEnum;
import net.peihuan.newblog.config.BlogProperties;
import net.peihuan.newblog.exception.BaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


@Slf4j
@Service
public class CdnService {

    @Autowired
    private BlogProperties blogProperties;

    @Async
    public void refreshHoleSite() {
        if (blogProperties.getAli().getCdn() == null
                || StringUtils.isEmpty(blogProperties.getAli().getCdn().getHost())
                || StringUtils.isEmpty(blogProperties.getAli().getCdn().getRegionId())) {
            log.info("-------------- 未配置cdn");
            return;
        }
        DefaultProfile profile = DefaultProfile.getProfile(
                blogProperties.getAli().getCdn().getRegionId(),             // 您的可用区ID
                blogProperties.getAli().getAccessKeyId(),         // 您的AccessKey ID
                blogProperties.getAli().getAccessKeySecret());    // 您的AccessKey Secret
        IAcsClient client = new DefaultAcsClient(profile);
        RefreshObjectCachesRequest request = new RefreshObjectCachesRequest();
        // 此参数为刷新的类型， 其值可以为File或Directory。默认值：File。
        request.setObjectType("Directory");
        // 加速的文件位置，wdtest.licai.cn为配置的域名,后加加速的文件名
        String objectPath = "https://" + blogProperties.getAli().getCdn().getHost() + "/";
        request.setObjectPath(objectPath);

        try {
            RefreshObjectCachesResponse response = client.getAcsResponse(request);
            log.info("--------------- 刷新cdn  {} refreshTaskId: {}", objectPath, response.getRefreshTaskId());
            // ......在这里处理自己的逻辑
        } catch (ServerException e) {
            log.error(e.getErrMsg(), e);
            throw new BaseException(ResultEnum.ALI_ERROR, e.getErrMsg());
        } catch (ClientException e) {
            throw new BaseException(ResultEnum.ALI_ERROR, e.getErrCode() + ": " + e.getErrMsg());
        }
    }
}
