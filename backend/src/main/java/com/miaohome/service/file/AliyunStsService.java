package com.miaohome.service.file;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.sts.model.v20150401.AssumeRoleRequest;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miaohome.config.StorageConfig;
import com.miaohome.dto.StsCredentialsResponse;
import com.miaohome.exception.BusinessException;
import com.miaohome.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 阿里云 STS 临时凭证签发
 * 通过 RAM 角色 AssumeRole 为前端直传 OSS 签发受限制的临时凭证。
 */
@Service
public class AliyunStsService {

    /** STS 服务地域（全球统一为 cn-hangzhou） */
    private static final String STS_REGION = "cn-hangzhou";

    /** 临时凭证有效期（秒） */
    private static final long DURATION_SECONDS = 3600L;

    /** 单文件上传大小上限（字节），50MB，与 multipart 配置保持一致 */
    private static final long MAX_UPLOAD_BYTES = 50L * 1024 * 1024;

    private final StorageConfig.AliyunOssProperties props;
    private final ObjectMapper objectMapper;

    public AliyunStsService(StorageConfig.AliyunOssProperties props, ObjectMapper objectMapper) {
        this.props = props;
        this.objectMapper = objectMapper;
    }

    /**
     * 签发前端直传凭证
     * @return 临时凭证及上传所需信息
     */
    public StsCredentialsResponse issueCredentials() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String dir = FileStorageService.PUBLIC_PREFIX + dateStr + "/";

        DefaultProfile profile = DefaultProfile.getProfile(
                STS_REGION, props.getAccessKeyId(), props.getAccessKeySecret());
        IAcsClient client = new DefaultAcsClient(profile);

        AssumeRoleRequest request = new AssumeRoleRequest();
        request.setMethod(MethodType.POST);
        request.setRoleArn(props.getRoleArn());
        request.setRoleSessionName(props.getRoleSessionName());
        request.setDurationSeconds(DURATION_SECONDS);
        request.setPolicy(buildPutPolicy(dir));

        try {
            AssumeRoleResponse response = client.getAcsResponse(request);
            AssumeRoleResponse.Credentials creds = response.getCredentials();

            StsCredentialsResponse dto = new StsCredentialsResponse();
            dto.setAccessKeyId(creds.getAccessKeyId());
            dto.setAccessKeySecret(creds.getAccessKeySecret());
            dto.setSecurityToken(creds.getSecurityToken());
            dto.setExpiration(creds.getExpiration());
            dto.setRegion(props.getRegion());
            dto.setBucket(props.getBucket());
            dto.setEndpoint(props.getEndpoint());
            dto.setDir(dir);
            return dto;
        } catch (ClientException e) {
            throw new BusinessException(ErrorCode.STS_ISSUE_ERROR,
                    "签发上传凭证失败: " + e.getErrMsg(), e);
        }
    }

    /**
     * 构建限制权限策略：仅允许向指定桶的指定目录前缀上传对象，并限制单文件大小
     */
    private String buildPutPolicy(String dir) {
        try {
            Map<String, Object> condition = Map.of(
                    "NumericLessThanEquals", Map.of("oss:ContentLength", MAX_UPLOAD_BYTES)
            );
            Map<String, Object> statement = Map.of(
                    "Effect", "Allow",
                    "Action", List.of("oss:PutObject"),
                    "Resource", List.of("acs:oss:*:*:" + props.getBucket() + "/" + dir + "*"),
                    "Condition", condition
            );
            Map<String, Object> policy = Map.of(
                    "Version", "1",
                    "Statement", List.of(statement)
            );
            return objectMapper.writeValueAsString(policy);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.STS_ISSUE_ERROR, "构建上传策略失败", e);
        }
    }
}
