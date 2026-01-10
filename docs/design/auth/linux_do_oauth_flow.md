[Skip to main content](https://linux.do/t/topic/231933#main-container)

[![LINUX DO](https://linux.do/uploads/default/original/4X/d/1/4/d146c68151340881c884d95e0da4acdf369258c6.png)](https://linux.do/)

[​](https://linux.do/search?expanded=true "Open advanced search")

​


Log In

- [社区子系统和元宇宙](https://linux.do/pub/resources "社区子系统和元宇宙")

- [Topics](https://linux.do/latest "All topics")
- [Upcoming events](https://linux.do/upcoming-events "Upcoming events")
- More


资源


- [Connect](https://connect.linux.do/)
- [Channel](https://t.me/linux_do_channel)
- [IDC Flare](https://idcflare.com/)

Categories


- [开发调优](https://linux.do/c/develop/4 "此版块包含开发、测试、调试、部署、优化、安全等方面的内容。")
- [国产替代](https://linux.do/c/domestic/98 "汇聚中国智造，推动技术自强。")
- [资源荟萃](https://linux.do/c/resource/14 "包括软件分享、开源仓库、视频课程、书籍等分享。")
- [网盘资源](https://linux.do/c/resource/cloud-asset/94 "网盘资源专用类别，主帖不限时编辑。")
- [文档共建](https://linux.do/c/wiki/42 "佬友化身翰林学士，一起来编书了。")
- [非我莫属](https://linux.do/c/job/27 "学成文武艺，货与帝王家。招聘/求职分类，只能发此类信息。")
- [读书成诗](https://linux.do/c/reading/32 "跟着佬友们一起在论坛读完一本书是什么体验？")
- [前沿快讯](https://linux.do/c/news/34 "前沿快讯，不出门能知天下事。")
- [网络记忆](https://linux.do/c/feeds/92 "网络是有记忆的，确信！")
- [福利羊毛](https://linux.do/c/welfare/36 "正经人谁花那个钱啊～ 此版块供羊毛、抽奖等福利使用。")
- [搞七捻三](https://linux.do/c/gossip/11 "闲聊吹水的板块。不得讨论政治、色情等违规内容。")
- [运营反馈](https://linux.do/c/feedback/2 "有关此网站、其组织、运作方式以及如何改进的讨论。")
- [All categories](https://linux.do/categories)

Tags


- [人工智能](https://linux.do/tag/%E4%BA%BA%E5%B7%A5%E6%99%BA%E8%83%BD "")
- [公告](https://linux.do/tag/%E5%85%AC%E5%91%8A "")
- [原创](https://linux.do/tag/%E5%8E%9F%E5%88%9B "高质量原创帖子（非AI生成、润色内容，非洗稿、搬运内容）可用。")
- [快问快答](https://linux.do/tag/%E5%BF%AB%E9%97%AE%E5%BF%AB%E7%AD%94 "")
- [抽奖](https://linux.do/tag/%E6%8A%BD%E5%A5%96 "")
- [精华神帖](https://linux.do/tag/%E7%B2%BE%E5%8D%8E%E7%A5%9E%E5%B8%96 "")
- [集中帖](https://linux.do/tag/%E9%9B%86%E4%B8%AD%E5%B8%96 "这里是一些社区开启的专题集中帖。&lt;br&gt;存在集中帖时，在帖子集中讨论，不要另开新帖。")
- [All tags](https://linux.do/tags)

Default


​

​


**真诚**、 **友善**、 **团结**、 **专业**，共建你我引以为荣之社区。HK节点当前缓慢。 [《常见问题解答》](https://linux.do/faq)

# [介绍OAuth2-（Springboot Vue实现LinuxDo第三方授权登录）](https://linux.do/t/topic/231933)

[开发调优](https://linux.do/c/develop/4)

[软件开发](https://linux.do/tag/%E8%BD%AF%E4%BB%B6%E5%BC%80%E5%8F%91)

You have selected **0** posts.

[select all](https://linux.do/t/topic/231933)

[cancel selecting](https://linux.do/t/topic/231933)

[Oct 2024](https://linux.do/t/topic/231933/1 "Jump to the first post")

2 / 24


Oct 2024


[May 2025](https://linux.do/t/topic/231933/24)

## post by dawnstar on Oct 14, 2024

[![](https://linux.do/user_avatar/linux.do/dawnstar/48/219555_2.png)](https://linux.do/u/dawnstar)

[东方既白](https://linux.do/u/dawnstar)[dawnstar](https://linux.do/u/dawnstar)
大预言家


1

[Oct 2024](https://linux.do/t/topic/231933 "Post date")

# [标题链接](https://linux.do/t/topic/231933\#p-2096637-h-1) 一、介绍

文档： [RFC 6749 - The OAuth 2.0 Authorization Framework](https://datatracker.ietf.org/doc/html/rfc6749)

参考： [让我们来共享论坛用户数据](https://linux.do/t/topic/29954)

## [标题链接](https://linux.do/t/topic/231933\#p-2096637-h-1-2) 1\. 角色

- **Client：客户端**
  - 定义：客户端是想要访问资源服务器上的用户资源的应用程序或服务。它可以是网页应用、移动应用、桌面应用，甚至是服务器端应用。
  - 职责：
    - 发起授权请求：客户端向资源所有者请求访问权限，并引导资源所有者通过授权服务器进行授权。
    - 持有凭证：客户端拥有一个唯一的客户端标识符（Client ID）和可能的客户端密钥（Client Secret），用于在与授权服务器通信时进行身份验证。
    - 使用访问令牌：一旦获得访问令牌，客户端使用该令牌向资源服务器请求受保护的资源。
- **User Agent：用户代理**
  - 定义：通常是用户使用的浏览器。
  - 职责：用户（资源所有者）通过用户代理使得客户端与授权服务器进行通信。
- **Resource Server：资源服务器**
  - 定义：资源服务器托管着受保护的资源（如用户数据等）。它负责根据客户端提供的访问令牌来授权或拒绝对这些资源的访问。
  - 职责：
    - 保护资源：确保只有经过授权的请求才能访问受保护的资源。
    - 验证访问令牌：在收到客户端的请求时，资源服务器验证访问令牌的有效性和权限范围。
    - 提供资源：一旦令牌被验证，资源服务器将相应的资源数据返回给客户端。
- **Authorization Server：授权服务器**
  - 定义：授权服务器负责处理客户端的授权请求，验证资源所有者的身份，并颁发访问令牌（Access Token）给客户端。
  - 职责：
    - 处理授权请求：接受客户端的授权请求，包括资源所有者的授权。
    - 验证身份：通过各种方式（如用户名/密码、多因素认证等）验证资源所有者的身份。
    - 颁发令牌：在授权成功后，生成并颁发访问令牌（以及可选的刷新令牌）给客户端。
    - 管理令牌生命周期：负责令牌的生成、验证、刷新和撤销。
- **Resource Owner：资源所有者**
  - 定义：资源所有者通常是最终用户，拥有受保护资源的访问权限。资源所有者通过授权服务器授权客户端访问其资源。
  - 职责：
    - 授权决策：决定是否允许客户端访问其受保护的资源。
    - 提供授权：通过与授权服务器的交互，给予或撤销客户端的访问权限。

以某论坛系统要使用Github OAuth2授权为例：

- **客户端Client** 是 **论坛系统**
- **用户代理UserAgent** 是 **浏览器**
- **资源所有者ResourceOwner** 是使用Github的 **用户**
- **授权服务器AuthorizationServer** 是 **Github的授权服务器**
- **资源服务器ResourceServer** 是 **Github保存用户资源的服务器**

## [标题链接](https://linux.do/t/topic/231933\#p-2096637-h-2-3) 2\. 交互流程

[![OAuth2-flow](https://linux.do/uploads/default/original/3X/8/a/8a643ec4823b97d604071ced8364803c12736f17.png)\\
OAuth2-flow766×411 9.47 KB](https://linux.do/uploads/default/original/3X/8/a/8a643ec4823b97d604071ced8364803c12736f17.png "OAuth2-flow")

第一张图片展示了基本的交互流程：

- AB客户端指的是UserAgent浏览器，CDEF客户端指的是Client应用程序。
- A：浏览器呈现给用户授权页面
- B：用户同意授权
- C：客户端发起一个授权请求
- D：授权成功后，授权服务器返回Access Token
- E：客户端携带Access Token，请求用户数据
- F：资源服务器传送回受保护的资源

在第二部分会展示更详细的流程。

## [标题链接](https://linux.do/t/topic/231933\#p-2096637-h-3-4) 3\. 创建应用程序

填写：

- 应用名称：无关紧要
- 应用描述：无关紧要
- 应用主页：首页地址
- 回调地址：是客户端应用在 OAuth 2.0 授权流程中预先注册并提供给授权服务器的一个 URL。当用户完成授权操作后，授权服务器会将用户代理重定向回这个地址，并附带必要的参数（如授权码或访问令牌）。得到code之后，会将用户带回 **回调页面**，将code发送给后端服务器，然后服务器请求授权服务器得到token

得到：

- Client id：客户端标识，唯一id
- Client Secret：不能泄露

# [标题链接](https://linux.do/t/topic/231933\#p-2096637-h-5) 二、授权码流程

[![image-20241014205722975](https://linux.do/uploads/default/optimized/3X/c/e/cea199a76e16ab867a60bc75ba85f5ac1935421c_2_690x423.png)\\
image-20241014205722975838×514 27.6 KB](https://linux.do/uploads/default/original/3X/c/e/cea199a76e16ab867a60bc75ba85f5ac1935421c.png "image-20241014205722975")

第二张图片展示了授权码模式授权的详细流程

- A：（在左下角）客户端通过将浏览器（用户代理）重定向到授权页面来启动授权流程。客户端在请求中包含Client Id, request scope, local state, redirection URI。授权服务器将在访问被授权（或拒绝）后，通过该 URI 将用户代理返回给客户端。
- B：授权服务器对资源所有者（用户）进行身份验证（通过用户代理），并确定资源所有者（用户）是授予还是拒绝客户端的访问请求。
- C：假设资源所有者授予访问权限，授权服务器使用先前提供的回调 URI（在请求中或在客户端注册期间）将用户代理重定向回客户端。回调URI 包括授权码code和客户端先前提供的任何本地状态。
- D：客户端通过包含在上一步中收到的授权码，从授权服务器的令牌端点请求访问令牌。发出请求时，客户端会向授权服务器进行身份验证。
- E：授权服务器对客户端进行身份验证，验证授权代码code，并确保收到的回调 URI 与步骤 (C) 中用于重定向客户端的 URI 匹配。如果有效，授权服务器将使用访问令牌和刷新令牌进行响应。
- 至此，得到AccessToken，便可以携带token来请求用户数据。

以某论坛系统要使用Github OAuth2授权为例：

1. 用户在论坛系统上选择使用Github登录：论坛系统（客户端）将用户的浏览器（用户代理）重定向到GitHub的授权端点，同时传递必要的信息（如客户端ID、重定向URI、请求的权限范围等）。
2. 用户在Github上授权：GitHub的授权服务器提示用户登录（如果尚未登录）并请求用户授权论坛系统访问其GitHub数据。
3. 用户授权后，Github重定向回论坛系统。如果用户同意授权，GitHub的授权服务器将生成一个授权码code，并通过浏览器将其发送回论坛系统指定的重定向URI。
4. 论坛系统通过授权码code请求访问令牌AccessToken。论坛系统向GitHub的令牌端点发送请求，交换授权码以获取访问令牌。（用code换取token）
5. 获取访问令牌AccessToken后，论坛系统访问用户的Github数据。最重要的是拿到openid（id是某个系统用户的唯一标识），能拿到id就能说明拿到了用户数据，授权成功。

# [标题链接](https://linux.do/t/topic/231933\#p-2096637-springboot-vuelinuxdo-6) 三、Springboot Vue实现LinuxDo授权登录

## [标题链接](https://linux.do/t/topic/231933\#p-2096637-h-1-7) 1\. 登录页面

界面

Vue登录界面

```vue
<div class="col text-center">
    <img src="@/assets/linuxdo.png" alt="" height="30px">
    <span @click="loginWithLinuxDo" style="cursor: pointer;">
      使用LinuxDo账号登录
    </span>
</div>
```

js

JS

```js
const CLIENT_ID = "xxxxxxxxxxxxxxxxxxx"; // 替换为你的client_id
const REDIRECT_URI = "http://xxxxx/xxxx/linux-do/callback"; // 替换为你的重定向URI
const AUTHORIZATION_ENDPOINT = "https://connect.linux.do/oauth2/authorize";
const loginWithLinuxDo = () => {
  const state = generateRandomString(16); // 生成随机状态参数以防止CSRF
  localStorage.setItem("oauth_state", state); // 存储状态以便回调时验证
  const authUrl = `${AUTHORIZATION_ENDPOINT}?response_type=code&client_id=${encodeURIComponent(
    CLIENT_ID
  )}&redirect_uri=${encodeURIComponent(
    REDIRECT_URI
  )}&state=${state}`;
  window.location.href = authUrl;
};

// 生成随机字符串
const generateRandomString = (length) => {
  const chars =
    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
  let result = "";
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
};
```

流程

- 点击登录
- 重定向到授权页面authUrl
- 在授权页面点击确认
- 进入到回调地址

## [标题链接](https://linux.do/t/topic/231933\#p-2096637-h-2-8) 2\. 回调页面

Vue回调页面

```vue
<!-- src/views/OAuthCallback.vue -->
<template>
  <div class="container">
    <h1>正在处理您的请求，请稍候。</h1>
  </div>
</template>

<script>
import { onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import userApi from '@/api/user' // 导入 userApi
import { useTokenStore } from '@/stores/token.js'

export default {
  name: 'OAuthCallback',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const tokenStore = useTokenStore()

    onMounted(async () => {
      const urlParams = new URLSearchParams(route.query)
      const code = urlParams.get('code')

      if (code) {
        try {
          // 发送POST请求到后端服务器
          const res = await userApi.loginLinuxDo(code)
          const tokenData = {
            token: '',
            refreshToken: ''
          }
          tokenData.token = res.data.token
          tokenData.refreshToken = res.data.refreshToken
          console.log(tokenData)
          if (tokenData.token != '') {
            // 存储token
            tokenStore.setToken(tokenData)

            // 如果需要存储用户信息

            // 重定向到首页并携带欢迎消息
            router.push("/")
          } else {
            // 处理没有token的情况
            console.error('No token received')
            router.push({ name: 'login', query: { error: '认证失败，请重试。' } })
          }
        } catch (error) {
          console.error('Error during OAuth callback processing:', error)
          router.push({ name: 'login', query: { error: '认证过程中发生错误，请重试。' } })
        }
      } else {
        console.error('No code found in URL')
        router.push({ name: 'login', query: { error: '缺少授权码，请重试。' } })
      }
    })

    return {}
  },
}
</script>

<style scoped>
略
</style>
```

流程：

- 拿到code
- 将code发送到服务器
- 后端服务器处理，（授权成功，得到AccessToken，得到用户数据），给用户本网站的AccessToken
- 用户登录成功

## [标题链接](https://linux.do/t/topic/231933\#p-2096637-h-3-9) 3\. 服务器处理

LinuxDoApiClient.java

- 根据code拿token
- 根据token拿用户数据

LinuxDoApiClient.java

```java
@Component
public class LinuxDoApiClient {
    private RestTemplate restTemplate = new RestTemplate();

    @Value("${login.LinuxDo.clientId}")
    private String clientId;

    @Value("${login.LinuxDo.clientSecret}")
    private String clientSecret;

    @Value("${login.LinuxDo.redirectUri}")
    private String redirectUri;

public String getTokenByCode(String code) {
    String tokenEndpoint = "https://connect.linux.do/oauth2/token";
    try {
        // 构建请求体
        StringBuilder params = new StringBuilder();
        params.append("grant_type=").append(URLEncoder.encode("authorization_code", "UTF-8"));
        params.append("&code=").append(URLEncoder.encode(code, StandardCharsets.UTF_8));
        params.append("&redirect_uri=").append(URLEncoder.encode(redirectUri, StandardCharsets.UTF_8));

        // 创建URL和连接
        URL url = new URL(tokenEndpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        // 设置请求头
        String credentials = clientId + ":" + clientSecret;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes("UTF-8"));
        conn.setRequestProperty("Authorization", "Basic " + encodedCredentials);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Accept", "application/json");

        // 发送请求体
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = params.toString().getBytes("UTF-8");
            os.write(input, 0, input.length);
        }

        // 读取响应
        int status = conn.getResponseCode();
        InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        // 解析响应
        String responseJson = response.toString();
        Map<String, Object> responseMap = JSONUtil.parseToMap(responseJson);
        Object accessToken = responseMap.get("access_token");
        if (accessToken == null) {
            String errorMessage = (String) responseMap.get("message");
            ExceptionTool.throwException("获取LinuxDoToken失败！错误信息：" + errorMessage);
        }
        return accessToken.toString();
    } catch (Exception e) {
        // 处理异常
        throw new RuntimeException("获取LinuxDoToken失败！", e);
    }
}

public Map<String, Object> getThirdUserInfo(String token) {
    String url = "https://connect.linux.do/api/user";
    try {
        URL obj = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
        conn.setRequestMethod("GET");

        // 设置Authorization头部
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Accept", "application/json");

        int responseCode = conn.getResponseCode();
        InputStream is = (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        // 解析响应
        String responseJson = response.toString();
        Map<String, Object> responseMap = JSONUtil.parseToMap(responseJson);

        Object openId = responseMap.get("id");
        if (openId == null) {
            // 可以根据需要抛出异常或返回特定错误信息
            throw new RuntimeException("获取用户信息失败，未找到id字段。");
        }

        HashMap<String, Object> thirdUser = new HashMap<>();
        thirdUser.put("openId", openId);
        thirdUser.put("nickname", responseMap.get("username")); // 使用username字段
        return thirdUser;
    } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("获取LinuxDo用户信息失败！", e);
    }
}
}
```

LinuxDoAuthentication.java

用于封装与 LinuxDo 认证相关的认证信息。

LinuxDoAuthentication.java

```java
public class LinuxDoAuthentication extends AbstractAuthenticationToken {
  @Getter
  @Setter
  private String code; // 前端传过来
  private UserLoginDTO currentUser; // 认证成功后，后台从数据库获取信息

  public LinuxDoAuthentication() {
    // 权限，用不上，直接null
    super(null);
  }

  @Override
  public Object getCredentials() {
    return isAuthenticated() ? null : code;
  }

  @Override
  public Object getPrincipal() {
    return isAuthenticated() ? currentUser : null;
  }

  public UserLoginDTO getCurrentUser() {
    return currentUser;
  }

  public void setCurrentUser(UserLoginDTO currentUser) {
    this.currentUser = currentUser;
  }
}
```

LinuxDoAuthenticationFilter.java

用于拦截特定的认证请求路径，并将请求中的认证信息提取出来，封装成 `LinuxDoAuthentication` 对象，交由 Spring Security进行认证处理。

LinuxDoAuthenticationFilter.java

```java
public class LinuxDoAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

  private static final Logger logger = LoggerFactory.getLogger(LinuxDoAuthenticationFilter.class);

  public LinuxDoAuthenticationFilter(AntPathRequestMatcher pathRequestMatcher,
                                     AuthenticationManager authenticationManager,
                                     AuthenticationSuccessHandler authenticationSuccessHandler,
                                     AuthenticationFailureHandler authenticationFailureHandler) {
    super(pathRequestMatcher);
    setAuthenticationManager(authenticationManager);
    setAuthenticationSuccessHandler(authenticationSuccessHandler);
    setAuthenticationFailureHandler(authenticationFailureHandler);
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request,
      HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
    logger.debug("use LinuxDoAuthenticationFilter");

    // 提取请求数据
    String requestJsonData = request.getReader().lines()
        .collect(Collectors.joining(System.lineSeparator()));
    Map<String, Object> requestMapData = JSONUtil.parseToMap(requestJsonData);
    String code = requestMapData.get("code").toString();

    // 封装成Spring Security需要的对象
    LinuxDoAuthentication authentication = new LinuxDoAuthentication();
    authentication.setCode(code);
    authentication.setAuthenticated(false);

    // 开始登录认证。SpringSecurity会利用 Authentication对象去寻找 AuthenticationProvider进行登录认证
    return getAuthenticationManager().authenticate(authentication);
  }

}
```

LinuxDoAuthenticationProvider.java

负责验证来自 `LinuxDoAuthentication` 认证令牌中的认证码，与LinuxDo平台交互获取用户信息，并将其与本地系统的用户数据进行匹配或创建。

LinuxDoAuthenticationProvider.java

```java
@Component
public class LinuxDoAuthenticationProvider implements AuthenticationProvider {

  @Autowired
  private UserService userService;

  @Autowired
  private LinuxDoApiClient linuxDoApiClient;

  public static final String PLATFORM = "LinuxDo";

  public LinuxDoAuthenticationProvider() {
    super();
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    String code = (String) authentication.getCredentials();
    System.out.println("LinuxDo code: " + code);
    try {
      String token = linuxDoApiClient.getTokenByCode(code);
      System.out.println("LinuxDo token: " + token);
      if (token == null) {
        // 乱传code过来。用户根本没授权！
        ExceptionTool.throwException("授权失败！");
      }
      System.out.println("LinuxDo thirdUser: 111");
      Map<String, Object> thirdUser = linuxDoApiClient.getThirdUserInfo(token);
      if (thirdUser == null) {
        // 未知异常。获取不到用户openId，也就无法继续登录了
        ExceptionTool.throwException("授权失败！");
      }
      assert thirdUser != null;
      String openId = thirdUser.get("openId").toString();
      // 通过第三方的账号唯一id，去匹配数据库中已有的账号信息
      User user = userService.getUserByOpenId(openId, PLATFORM);
      boolean notBindAccount = user == null; // linuxdo账号没有绑定我们系统的用户
      if (notBindAccount) {
        // 没找到账号信息，那就是第一次使用linuxdo登录，可能需要创建一个新用户
        user = new User();
        userService.createUserWithOpenId(user, openId, PLATFORM);
        user = userService.getUserByOpenId(openId, PLATFORM);
      }
      LinuxDoAuthentication successAuth = new LinuxDoAuthentication();
      successAuth.setCurrentUser(JSONUtil.convert(user, UserLoginDTO.class));
      successAuth.setAuthenticated(true); // 认证通过，一定要设成true

      HashMap<String, Object> loginDetail = new HashMap<>();
      // 第一次使用三方账号登录，需要告知前端，让前端跳转到初始化账号页面（可能需要）
      loginDetail.put("needInitUserInfo", notBindAccount);
      loginDetail.put("nickname", thirdUser.get("nickname").toString()); // sayHello
      successAuth.setDetails(loginDetail);
      return successAuth;
    } catch (BaseException e) {
      // 转换已知异常，将异常内容返回给前端
      throw new BadCredentialsException(e.getMessage());
    } catch (Exception e) {
      // 未知异常
      throw new BadCredentialsException("LinuxDo Authentication Failed");
    }
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return authentication.isAssignableFrom(LinuxDoAuthentication.class);
  }
}
```

添加LinuxDo登录方式

SecurityConfig.java

```java
// 加一个登录方式。linuxdo 登录
LinuxDoAuthenticationFilter giteeFilter = new LinuxDoAuthenticationFilter(
        new AntPathRequestMatcher("/public/user/login/linuxdo", HttpMethod.POST.name()),
        new ProviderManager(
                List.of(applicationContext.getBean(LinuxDoAuthenticationProvider.class))),
        loginSuccessHandler,
        loginFailHandler);
http.addFilterBefore(giteeFilter, UsernamePasswordAuthenticationFilter.class);
```

# [标题链接](https://linux.do/t/topic/231933\#p-2096637-h-10) 四、最终效果

点击LinuxDo登录

[![image](https://linux.do/uploads/default/original/3X/9/6/96e21db41eaa256035b50dd70669f53180000011.jpeg)\\
image578×470 40 KB](https://linux.do/uploads/default/original/3X/9/6/96e21db41eaa256035b50dd70669f53180000011.jpeg "image")

点击允许

[![image](https://linux.do/uploads/default/original/3X/0/a/0ae2f36451357d5b846e9067556e57a77e7f743a.png)\\
image525×447 15.4 KB](https://linux.do/uploads/default/original/3X/0/a/0ae2f36451357d5b846e9067556e57a77e7f743a.png "image")

进入回调页面

[![image](https://linux.do/uploads/default/optimized/3X/9/8/987863e941a6031b4f1b15e55007d456214af294_2_690x464.png)\\
image1347×906 12.8 KB](https://linux.do/uploads/default/original/3X/9/8/987863e941a6031b4f1b15e55007d456214af294.png "image")

登录成功，进入主页。成功获取用户信息。

[![image](https://linux.do/uploads/default/original/3X/a/0/a02b96990853dd27f5fca51ce310e7838041ab97.png)\\
image314×284 2.27 KB](https://linux.do/uploads/default/original/3X/a/0/a02b96990853dd27f5fca51ce310e7838041ab97.png "image")

4 Replies

![heart](https://linux.do/images/emoji/twemoji/heart.png?v=15)

heart

![+1](https://linux.do/images/emoji/twemoji/+1.png?v=15)

+1

65


​


​


- [ASP.NET Core Identity 接入 LINUX DO Connect 登录（使用 Blazor 模板）](https://linux.do/t/topic/988869)

1.5k
views
118
likes
3
links
17
users


[![](https://linux.do/user_avatar/linux.do/dawnstar/48/219555_2.png)5](https://linux.do/u/dawnstar "dawnstar")

[![](https://linux.do/user_avatar/linux.do/qaws12g/48/306051_2.png)2](https://linux.do/u/QAWS12g "QAWS12g")

[![](https://linux.do/user_avatar/linux.do/akuowen/48/227841_2.png)2](https://linux.do/u/akuowen "akuowen")

[![](https://linux.do/letter_avatar/linzi/48/5_c16b2ee14fe83ed9a59fc65fbec00f85.png)2](https://linux.do/u/linzi "linzi")

[![](https://linux.do/user_avatar/linux.do/demain/48/676950_2.png)](https://linux.do/u/demain "demain")

read
5
min


## post by handsome on Oct 14, 2024

[![](https://linux.do/user_avatar/linux.do/handsome/48/1334482_2.png)](https://linux.do/u/handsome)

[大帅哥](https://linux.do/u/handsome)[handsome](https://linux.do/u/handsome)
种子用户
![rage](https://linux.do/images/emoji/twemoji/rage.png?v=15)

[Oct 2024](https://linux.do/t/topic/231933/2 "Post date")

好强！感谢大佬！

![heart](https://linux.do/images/emoji/twemoji/heart.png?v=15)

heart

![laughing](https://linux.do/images/emoji/twemoji/laughing.png?v=15)

laughing

5


​


​


## post by ehzyil on Oct 14, 2024

[![](https://linux.do/user_avatar/linux.do/ehzyil/48/46547_2.png)](https://linux.do/u/ehzyil)

[ehzyil](https://linux.do/u/ehzyil)
浴火重生


[Oct 2024](https://linux.do/t/topic/231933/3 "Post date")

mark 感谢分享

4


​


​


## post by 1-debtor on Oct 14, 2024

[![](https://linux.do/user_avatar/linux.do/1-debtor/48/324608_2.gif)](https://linux.do/u/1-debtor)

[1-debtor](https://linux.do/u/1-debtor)
蛇来运转


[Oct 2024](https://linux.do/t/topic/231933/4 "Post date")

Mark 感谢

4


​


​


## post by Chenpeel on Oct 14, 2024

[![](https://linux.do/user_avatar/linux.do/chenpeel/48/252029_2.png)](https://linux.do/u/chenpeel)

[Chenpeel](https://linux.do/u/chenpeel)
蛇来运转
![speech_balloon](https://linux.do/images/emoji/twemoji/speech_balloon.png?v=15)

[Oct 2024](https://linux.do/t/topic/231933/5 "Post date")

mark感谢分享

3


​


​


## post by demain on Oct 14, 2024

[![](https://linux.do/user_avatar/linux.do/demain/48/676950_2.png)](https://linux.do/u/demain)

[明天](https://linux.do/u/demain)[demain](https://linux.do/u/demain)
活跃用户


[Oct 2024](https://linux.do/t/topic/231933/6 "Post date")

感谢 大佬分享

2


​


​


## post by seven on Oct 14, 2024

[![](https://linux.do/user_avatar/linux.do/seven/48/11125_2.png)](https://linux.do/u/seven)

[seven](https://linux.do/u/seven)
Regular


[Oct 2024](https://linux.do/t/topic/231933/7 "Post date")

正经的技术教程贴，居然这么少回复

2


​


​


## post by crazyet on Oct 14, 2024

[![](https://linux.do/user_avatar/linux.do/crazyet/48/596_2.png)](https://linux.do/u/crazyet)

[crazyet](https://linux.do/u/crazyet)
种子用户
![bili_001](https://linux.do/uploads/default/original/3X/4/7/47438f4f7b27b13d4ec57b37ec7b7dcf5217de69.png?v=15)

[Oct 2024](https://linux.do/t/topic/231933/8 "Post date")

先赞后看!!

2


​


​


## post by akuowen on Oct 14, 2024

[![](https://linux.do/user_avatar/linux.do/akuowen/48/227841_2.png)](https://linux.do/u/akuowen)

[akuowen](https://linux.do/u/akuowen)

[Oct 2024](https://linux.do/t/topic/231933/9 "Post date")

![](https://linux.do/user_avatar/linux.do/dawnstar/48/219555_2.png) 东方既白:

[go to the quoted post](https://linux.do/t/topic/231933 "go to the quoted post")

> # 二、授权码流程
>
> [![image-20241014205722975](https://linux.do/uploads/default/optimized/3X/c/e/cea199a76e16ab867a60bc75ba85f5ac1935421c_2_690x423.png)\\
> image-20241014205722975838×514 27.6 KB](https://linux.do/uploads/default/original/3X/c/e/cea199a76e16ab867a60bc75ba85f5ac1935421c.png "image-20241014205722975")

这个图是咋画的呢

1 Reply

2


​


​


## post by yiwanyang on Oct 14, 2024

[![](https://linux.do/user_avatar/linux.do/yiwanyang/48/306375_2.gif)](https://linux.do/u/yiwanyang)

[一碗杨](https://linux.do/u/yiwanyang)[yiwanyang](https://linux.do/u/yiwanyang)
文化宣导员


[Oct 2024](https://linux.do/t/topic/231933/10 "Post date")

有用，学习一下

3


​


​


## post by dawnstar on Oct 14, 2024

[![](https://linux.do/user_avatar/linux.do/dawnstar/48/219555_2.png)](https://linux.do/u/dawnstar)

[东方既白](https://linux.do/u/dawnstar)[dawnstar](https://linux.do/u/dawnstar)
大预言家


[![](https://linux.do/user_avatar/linux.do/akuowen/24/227841_2.png)akuowen](https://linux.do/t/topic/231933 "post.in_reply_to")

[Oct 2024](https://linux.do/t/topic/231933/11 "Post date")

这个图是在官方文档里截下来的

![](https://linux.do/uploads/default/original/3X/a/3/a36eaf5ce9c4569f08c317d6e9adb3a1684222ae.png)[IETF Datatracker](https://datatracker.ietf.org/doc/html/rfc6749)

![](https://linux.do/uploads/default/optimized/3X/f/0/f055836e442579eb80d594c155b6ccf7c2677a1d_2_690x362.png)

### [RFC 6749: The OAuth 2.0 Authorization Framework](https://datatracker.ietf.org/doc/html/rfc6749)

The OAuth 2.0 authorization framework enables a third-party application to obtain limited access to an HTTP service, either on behalf of a resource owner by orchestrating an approval interaction between the resource owner and the HTTP service, or by...

1 Reply

3


​


​


## post by akuowen on Oct 16, 2024

[![](https://linux.do/user_avatar/linux.do/akuowen/48/227841_2.png)](https://linux.do/u/akuowen)

[akuowen](https://linux.do/u/akuowen)

[![](https://linux.do/user_avatar/linux.do/dawnstar/24/219555_2.png)东方既白](https://linux.do/t/topic/231933 "post.in_reply_to")

[Oct 2024](https://linux.do/t/topic/231933/12 "Post date")

hhhh 好吧 谢谢

3


​


​


## post by zhangyuan9888888 on Oct 17, 2024

[![](https://linux.do/user_avatar/linux.do/zhangyuan9888888/48/231974_2.png)](https://linux.do/u/zhangyuan9888888)

[火星科技](https://linux.do/u/zhangyuan9888888)[zhangyuan9888888](https://linux.do/u/zhangyuan9888888)
一元复始


[Oct 2024](https://linux.do/t/topic/231933/13 "Post date")

![](https://linux.do/user_avatar/linux.do/dawnstar/48/219555_2.png) 东方既白:

[go to the quoted post](https://linux.do/t/topic/231933 "go to the quoted post")

> ```javascript
>   LinuxDoAuthentication successAuth = new LinuxDoAuthentication();
>       successAuth.setCurrentUser(JSONUtil.convert(user, UserLoginDTO.class));
>       successAuth.setAuthenticated(true); // 认证通过，一定要设成true
>
>       HashMap<String, Object> loginDetail = new HashMap<>();
>       // 第一次使用三方账号登录，需要告知前端，让前端跳转到初始化账号页面（可能需要）
>       loginDetail.put("needInitUserInfo", notBindAccount);
>       loginDetail.put("nickname", thirdUser.get("nickname").toString()); // sayHello
>       successAuth.setDetails(loginDetail);
>       return successAuth;
> ```

这里不需要生成自己系统的token，返回给前端吗

2 Replies

2


​


​


## post by dawnstar on Oct 17, 2024

[![](https://linux.do/user_avatar/linux.do/dawnstar/48/219555_2.png)](https://linux.do/u/dawnstar)

[东方既白](https://linux.do/u/dawnstar)[dawnstar](https://linux.do/u/dawnstar)
大预言家


[![](https://linux.do/user_avatar/linux.do/zhangyuan9888888/24/231974_2.png)火星科技](https://linux.do/t/topic/231933 "post.in_reply_to")

[Oct 2024](https://linux.do/t/topic/231933/14 "Post date")

![](https://linux.do/user_avatar/linux.do/dawnstar/48/219555_2.png) 东方既白:

[go to the quoted post](https://linux.do/t/topic/231933 "go to the quoted post")

> SecurityConfig.java

需要的，我是在SpringSecurity的配置里写了关于登录成功的处理。

LoginSuccessHandler.java

```auto
@Component
public class LoginSuccessHandler extends
    AbstractAuthenticationTargetUrlRequestHandler implements AuthenticationSuccessHandler {

  @Autowired
  private ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  private JwtService jwtService;

  @Autowired
  private StringRedisTemplate stringRedisTemplate;

  public LoginSuccessHandler() {
    this.setRedirectStrategy(new RedirectStrategy() {
      @Override
      public void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url)
          throws IOException {
        // 更改重定向策略，前后端分离项目，后端使用RestFul风格，无需做重定向
        // Do nothing, no redirects in REST
      }
    });
  }

  @Override
  public void onAuthenticationSuccess(
          HttpServletRequest request,
          HttpServletResponse response,
          Authentication authentication
  ) throws IOException {
    Object principal = authentication.getPrincipal();
    if (principal == null || !(principal instanceof UserLoginDTO)) {
      ExceptionTool.throwException("登陆认证成功后，authentication.getPrincipal()返回的Object对象必须是：UserLoginInfo！");
    }
    UserLoginDTO currentUser = (UserLoginDTO) principal;
    currentUser.setSessionId(UUID.randomUUID().toString());

    // 生成token和refreshToken
    Map<String, Object> responseData = new LinkedHashMap<>();
    responseData.put("token", generateToken(currentUser));
    responseData.put("refreshToken", generateRefreshToken(currentUser));
    // 将refreshToken存放到Redis中，并设置过期时间
    stringRedisTemplate.opsForValue().set(USER_REFRESH_TOKEN_PREFIX + currentUser.getEmail(), responseData.get("refreshToken").toString(), 30, TimeUnit.DAYS);
    // 一些特殊的登录参数。比如三方登录，需要额外返回一个字段是否需要跳转的绑定已有账号页面
    Object details = authentication.getDetails();
    if (details instanceof Map) {
      Map detailsMap = (Map)details;
      responseData.putAll(detailsMap);
    }

    // 虽然APPLICATION_JSON_UTF8_VALUE过时了，但也要用。因为Postman工具不声明utf-8编码就会出现乱码
    response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
    PrintWriter writer = response.getWriter();
    writer.print(JSONUtil.stringify(Result.success(responseData)));
    writer.flush();
    writer.close();
  }

  public String generateToken(UserLoginDTO currentUser) {
    // todo 10分种
    long expiredTime = TimeTool.nowMilli() + TimeUnit.MINUTES.toMillis(10); // 10分钟后过期
    currentUser.setExpiredTime(expiredTime);
    return jwtService.createJwt(currentUser, expiredTime);
  }

  private String generateRefreshToken(UserLoginDTO loginInfo) {
    return jwtService.createJwt(loginInfo, TimeTool.nowMilli() + TimeUnit.DAYS.toMillis(30));
  }

}
```

3


​


​


## post by linzi on Oct 17, 2024

[![](https://linux.do/letter_avatar/linzi/48/5_c16b2ee14fe83ed9a59fc65fbec00f85.png)](https://linux.do/u/linzi)

[linzi](https://linux.do/u/linzi)

[Oct 2024](https://linux.do/t/topic/231933/15 "Post date")

感谢佬的分享![:clap:](https://linux.do/images/emoji/twemoji/clap.png?v=12)

2


​


​


## post by dawnstar on Oct 17, 2024

[![](https://linux.do/user_avatar/linux.do/dawnstar/48/219555_2.png)](https://linux.do/u/dawnstar)

[东方既白](https://linux.do/u/dawnstar)[dawnstar](https://linux.do/u/dawnstar)
大预言家


[![](https://linux.do/user_avatar/linux.do/zhangyuan9888888/24/231974_2.png)火星科技](https://linux.do/t/topic/231933 "post.in_reply_to")

[Oct 2024](https://linux.do/t/topic/231933/16 "Post date")

完整的代码可以参考这个

![](https://linux.do/uploads/default/original/3X/9/9/99f6c282c68a103749b8c2dbe39c2abc4685650b.png)[Gitee](https://gitee.com/LuPangJieDeng/spring-security-6-demo)

### [allen/spring-security-6-demo](https://gitee.com/LuPangJieDeng/spring-security-6-demo)

SpringSecurity6使用示例

1 Reply

2


​


​


## post by ziyou on Oct 17, 2024

[![](https://linux.do/user_avatar/linux.do/ziyou/48/584648_2.png)](https://linux.do/u/ziyou)

[AI](https://linux.do/u/ziyou)[ziyou](https://linux.do/u/ziyou)

[Oct 2024](https://linux.do/t/topic/231933/17 "Post date")

动手能力真不错

1


​


​


## post by cccL on Oct 25, 2024

[![](https://linux.do/user_avatar/linux.do/cccl/48/307594_2.gif)](https://linux.do/u/cccl)

[cccL](https://linux.do/u/cccl)
活跃用户


[Oct 2024](https://linux.do/t/topic/231933/18 "Post date")

有空的时候看看代码先 ![:see_no_evil:](https://linux.do/images/emoji/twemoji/see_no_evil.png?v=12)

1


​


​


## post by QAWS12g on Oct 25, 2024

[![](https://linux.do/user_avatar/linux.do/qaws12g/48/306051_2.gif)](https://linux.do/u/qaws12g)

[jiangly](https://linux.do/u/qaws12g)[QAWS12g](https://linux.do/u/qaws12g)
文化宣导员
![tada](https://linux.do/images/emoji/twemoji/tada.png?v=15)

[Oct 2024](https://linux.do/t/topic/231933/19 "Post date")

大佬方便分享源码吗，仅作为学习使用

1 Reply

2


​


​


## post by linzi on Oct 25, 2024

[![](https://linux.do/letter_avatar/linzi/48/5_c16b2ee14fe83ed9a59fc65fbec00f85.png)](https://linux.do/u/linzi)

[linzi](https://linux.do/u/linzi)

[![](https://linux.do/user_avatar/linux.do/dawnstar/24/219555_2.png)东方既白](https://linux.do/t/topic/231933 "post.in_reply_to")

[Oct 2024](https://linux.do/t/topic/231933/20 "Post date")

感谢佬的分享

2


​


​


## Load more posts below

Invalid date


Invalid date


Checking your Browser…

Verifying...

Stuck here? [Send Feedback](https://challenges.cloudflare.com/cdn-cgi/challenge-platform/h/g/turnstile/f/ov2/av0/rch/t1crd/0x4AAAAAAAc2BQjPEms9tKlM/auto/fbE/new/normal?lang=auto#refresh)

Success!

Error

Having trouble? [Send Feedback](https://challenges.cloudflare.com/cdn-cgi/challenge-platform/h/g/turnstile/f/ov2/av0/rch/t1crd/0x4AAAAAAAc2BQjPEms9tKlM/auto/fbE/new/normal?lang=auto#refresh)

Expired. [Refresh](https://challenges.cloudflare.com/cdn-cgi/challenge-platform/h/g/turnstile/f/ov2/av0/rch/t1crd/0x4AAAAAAAc2BQjPEms9tKlM/auto/fbE/new/normal?lang=auto#refresh)

Timed out. [Refresh](https://challenges.cloudflare.com/cdn-cgi/challenge-platform/h/g/turnstile/f/ov2/av0/rch/t1crd/0x4AAAAAAAc2BQjPEms9tKlM/auto/fbE/new/normal?lang=auto#refresh)

[Privacy](https://www.cloudflare.com/privacypolicy/) • [Terms](https://www.cloudflare.com/website-terms/)

StripeM-Inner