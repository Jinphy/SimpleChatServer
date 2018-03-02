/**
 *   ===========================================================================================
 *   ||----------------------- 请求接口文档 ----------------------------------------------------||
 *   ===========================================================================================
 *
 *   注：所有请求接口返回数据都包含code 和 msg 两个字段
 *
 *   --------------------------------------------------------------------------------------------
     DESC: 登录接口：
            ☆接口：/user/login
            ☆请求方式：Post
            ☆入参：
                字段：                 是否必传：           描述
                1、account:            是                  登录账号
                2、password：          否                  登录密码，md5加密，不传时需要用验证码验证登录
                3、deviceId:           是                  设备唯一识别码，md5加密
            ☆出参：
                1、account:            是                  账号
                2、name;               是                  昵称
                3、date                是                  注册日期
                4、sex                 是                  性别
                5、avatarUrl           是                  头像
                6、status              是                  登录状态
                7、accessToken         是                  账号认证令牌
                8、signature           是                  个性签名
                9、address             是                  地址
     --------------------------------------------------------------------------------------------
     DESC: 登出接口
            ☆接口：/user/logout
            ☆请求方式：Post
            ☆入参：
                字段：                 是否必传：           描述
                1、account：           是                   账号
                2、accessToken         是                   密码
            ☆出参
                1、account             是                   账号
                2、status              是                   登录状态
                3、accessToken         是                   账号认证令牌

     --------------------------------------------------------------------------------------------
     DESC: 查找用户是否穿在接口
            ☆接口：/user/findUser
            ☆请求方式：Get
            ☆入参：
                字段：                 是否必传：           描述
                1、account：           是                   账号
            ☆出参
     --------------------------------------------------------------------------------------------

     DESC: 创建用户接口
            ☆接口：/user/signUp
            ☆请求方式：Post
            ☆入参：
                字段：                 是否必传：           描述
                1、account:            是                  要创建的账号
                2、password:           是                  用户密码
                3、date:               是                  创建日期
            ☆出参：
     --------------------------------------------------------------------------------------------

      DESC: 修改用户信息接口
            ☆接口：/user/modifyUserInfo
            ☆请求方式：Post
            ☆入参：
                字段：                 是否必传：           描述
                1、account:            是                  要创建的账号
                2、accessToken:        是                  账号认证令牌
                3、deviceId            是                  设备唯一标识
                4、avatar     :        否                  头像
                5、name                否                  昵称
                6、signature           否                  个性签名
                7、password            否                  密码
                8、sex                 否                  性别
                9、address             否                  地址
            ☆出参：
                1、account             是                  账号
                2、accessToken         是                  账号认证令牌
                3、avatar              否                  头像
                4、name                否                  昵称
                5、signature           否                  个性签名
                7、sex                 否                  性别
                8、address             否                  地址
      --------------------------------------------------------------------------------------------


      DESC: 添加好友接口
            ☆接口：/friend/addFriend
            ☆请求方式：Post
            ☆入参：
                字段：                 是否必传：           描述
                1、requestAccount      是                  发起添加好友的请求方账号
                2、receiveAccount      是                  接收添加好友的接受方账号
                3、remark              否                  请求方对接收方的账号备注信息
                4、verifyMsg           否                  验证信息
                5、confirm             否                  确认信息 0表示拒绝，1表示同意，该字段在发起申请时不用传递，在是否接受时才传
                6、date                否                  成为好友的日期，该字段在发起申请时不用传递，在接受时才传
            ☆出参：
      --------------------------------------------------------------------------------------------



       DESC: 加载好友接口
                ☆接口：/friend/loadFriends
                ☆请求方式：Post
                ☆入参：
                    字段：                 是否必传：           描述
                    1、owner               是                  好友的拥有者
                ☆出参：
                    字段：
                    1、account             是                  好友对应的账号
                    2、name                是                  好友对应的昵称
                    3、avatar              是                  好友对应的头像
                    4、remark              是                  好友对应的备注
                    5、sex                 是                  好友对应的性别
                    6、address             是                  好友对应的地址
                    7、date                是                  成为好友的日期
                    8、owner               是                  好友的拥有者
                    9、status              是                  好友的状态
       --------------------------------------------------------------------------------------------


        DESC: 加载好友接口
                ☆接口：/friend/getFriend
                ☆请求方式：Post
                ☆入参：
                    字段：                 是否必传：           描述
                    1、owner               是                  好友的拥有者
                    2、account             是                  好友对应的账号
                ☆出参：
                    字段：
                    1、account             是                  好友对应的账号
                    2、name                是                  好友对应的昵称
                    3、avatar              是                  好友对应的头像
                    4、remark              是                  好友对应的备注
                    5、sex                 是                  好友对应的性别
                    6、address             是                  好友对应的地址
                    7、date                是                  成为好友的日期
                    8、owner               是                  好友的拥有者
                    9、status              是                  好友的状态
        -------------------------------------


       DESC: 添加好友接口
                ☆接口：/user/getAvatar
                ☆请求方式：Post
                ☆入参：
                    字段：                 是否必传：           描述
                    1、account             是                  头像对应的账号
                ☆出参：
                    字段：
                    1、avatar              是                   头像的base64编码字符串
       --------------------------------------------------------------------------------------------



        DESC: 添加好友接口
                ☆接口：/friend/modifyRemark
                ☆请求方式：Post
                ☆入参：
                    字段：                 是否必传：           描述
                    1、account             是                  好友对应的账号
                    2、owner               是                  好友的拥有者
                    3、remark              是                  备注信息
                ☆出参：
                    字段：
        --------------------------------------------------------------------------------------------


        DESC: 添加好友接口
                ☆接口：/friend/modifyStatus
                ☆请求方式：Post
                ☆入参：
                    字段：                 是否必传：           描述
                    1、account             是                  好友对应的账号
                    2、owner               是                  好友的拥有者
                    3、status              是                  好友状态
                ☆出参：
                    字段：
        --------------------------------------------------------------------------------------------


        DESC: 添加好友接口
                ☆接口：/friend/deleteFriend
                ☆请求方式：Post
                ☆入参：
                    字段：                 是否必传：           描述
                    1、account             是                  好友对应的账号
                    2、owner               是                  好友的拥有者
                ☆出参：
                    字段：
 --------------------------------------------------------------------------------------------








 * */