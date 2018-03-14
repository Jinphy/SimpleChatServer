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


        DESC: 获取好友接口
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


       DESC: 获取头像接口
                ☆接口：/user/getAvatar
                ☆请求方式：Post
                ☆入参：
                    字段：                 是否必传：           描述
                    1、account             是                  头像对应的账号
                ☆出参：
                    字段：
                    1、avatar              是                   头像的base64编码字符串
       --------------------------------------------------------------------------------------------


        DESC: 加载头像接口（不用这个，用上面的）
                ☆接口：/user/loadAvatars
                ☆请求方式：Post
                ☆入参：
                    字段：                 是否必传：           描述
                    1、accounts            是                  头像对应的账号（json数组）
                ☆出参：
                    字段：
                    1、avatar              是                   头像的base64编码字符串
                    2、account             是                   如果是好友头像则为好友账号，如果是群头像则为群号groupNo
        --------------------------------------------------------------------------------------------



        DESC: 修改好友备注接口
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


        DESC: 修改好友状态接口
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


        DESC: 删除好友接口
                ☆接口：/friend/deleteFriend
                ☆请求方式：Post
                ☆入参：
                    字段：                 是否必传：           描述
                    1、account             是                  好友对应的账号
                    2、owner               是                  好友的拥有者
                ☆出参：
                    字段：
 --------------------------------------------------------------------------------------------



        DESC: 检测账号接口
                ☆接口：/user/checkAccount
                ☆请求方式：Post
                ☆入参：
                    字段：                 是否必传：           描述
                    1、account             是                  用户的账号
                    2、accessToken         是                  账号令牌
                ☆出参：
                    字段：
                    1、account             是                  用户账号
                    2、accessToken         否                  返回新的账号令牌，都账号有效时才返回
        --------------------------------------------------------------------------------------------



        DESC: 创建群聊接口
                ☆接口：/group/createGroup
                ☆请求方式：Post
                ☆入参：
                    字段：                 是否必传：           描述
                    1、name                是                  群聊的名称
                    2、accessToken         是                  账号令牌
                    3、autoAdd             是                  是否自动添加成员
                    4、maxCount            是                  最大成员数
                    5、members             是                  群成员，以json数组的格式传递
                    6、avatar              否                  群头像，base64格式
                    7、creator             是                  群的创建者
                ☆出参：
                    字段：
                    1、creator             是                  群的创建者
                    2、owner               是                  群的拥有者
                    3、name                是                  群的名称
                    4、groupNo             是                  群号
                    6、maxCount            是                  群的最大成员数
                    7、autoAdd             是                  是否自动添加成员
                    8、showMemberName      是                  是否显示群成员的昵称（在聊天中）
                    9、keepSilent          是                  是否消息免打扰
                    10、rejectMsg          是                  是否拒接群消息
        --------------------------------------------------------------------------------------------


        DESC: 获取群聊接口
                ☆接口：/group/getGroups
                ☆请求方式：Post
                ☆入参：
                    字段：                 是否必传：           描述
                    1、groupNo            否                  群号，如果不传则返回群数组，否则返回单个群
                    2、owner              是                  群的拥有者
                    3、text               否                  该字段与1、2字段独立，当传该字段时，表示搜索于该字段名字或者群号相符的群聊
                ☆出参：
                    字段：
                    1、creator             是                  群的创建者
                    2、owner               是                  群的拥有者
                    3、name                是                  群的名称
                    4、groupNo             是                  群号
                    6、maxCount            是                  群的最大成员数
                    7、autoAdd             是                  是否自动添加成员
                    8、showMemberName      是                  是否显示群成员的昵称（在聊天中）
                    9、keepSilent          是                  是否消息免打扰
                    10、rejectMsg          是                  是否拒接群消息
        --------------------------------------------------------------------------------------------




        DESC: 修改群聊信息接口
                ☆接口：/group/modifyGroup
                ☆请求方式：Post
                ☆入参：
                    字段：                 是否必传：           描述
                    1、groupNo              是                  群号，不能更改
                    2、creator              否                  群的创建者，不能更改
                    3、owner                是                  群的拥有者，不能更改
                    4、name                 否                  群名，更改需通知成员
                    5、avatar               否                  群头像，更改需通知成员
                    6、maxCount             否                  最大成员数,需要传creator，更改需通知成员
                    7、autoAdd              否                  成员自动加入,需要传creator，更改需通知成员
                    8、showMemberName       否                  是否显示成员名称，更改无需通知成员
                    9、keepSilent           否                  消息免打扰，更改无需通知成员
                    10、rejectMsg           否                  拒绝接收群消息，更改无需通知成员
                ☆出参：
                    字段：
        --------------------------------------------------------------------------------------------


        DESC: 申请加入群聊接口
                ☆接口：/group/joinGroup
                ☆请求方式：Post
                ☆入参：
                    字段：                 是否必传：           描述
                    1、groupNo             是                  群号
                    2、creator             是                  群聊的创建者
                    3、account             是                  申请加入群聊的账号
                    4、extraMsg            否                  验证信息，当加入群聊需要群主验证时才需要传
                ☆出参：
                    字段：
        --------------------------------------------------------------------------------------------




        DESC: 同意申请加入群聊接口
                ☆接口：/group/agreeJoinGroup
                ☆请求方式：Post
                ☆入参：
                    字段：                 是否必传：           描述
                    1、groupNo             是                  群号
                    2、creator             是                  群聊的创建者
                    3、account             是                  申请加入群聊的账号
                ☆出参：
                    字段：
        --------------------------------------------------------------------------------------------



        DESC: 退出群聊或者解散群聊接口·
                ☆接口：/group/exitGroup
                ☆请求方式：Post
                ☆入参：
                    字段：                 是否必传：           描述
                    1、groupNo             是                  群号
                    2、account             否                  申请加入群聊的账号，传则删除成员，不传则解散群
                    3、operator            是                  操作者账号
                ☆出参：
                    字段：
        --------------------------------------------------------------------------------------------


        DESC: 获取群成员接口
                ☆接口：/member/getMembers
                ☆请求方式：Post
                ☆入参：
                    字段：                 是否必传：           描述
                    1、groupNos            否                  群号
                ☆出参：
                    字段：
                    1、groupNo             是                  群号
                    2、account             是                  成员对应的账号
                    3、allowChat           是                  是否允许聊天
                    4、status              是                  成员状态
        --------------------------------------------------------------------------------------------







 * */