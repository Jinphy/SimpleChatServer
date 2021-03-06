package com.jinphy.simplechatserver.network;

import com.jinphy.simplechatserver.annotation.Path;
import com.jinphy.simplechatserver.constants.StringConst;
import com.jinphy.simplechatserver.database.dao.*;
import com.jinphy.simplechatserver.database.models.Result;
import com.jinphy.simplechatserver.models.db_models.*;
import com.jinphy.simplechatserver.models.network_models.KeyValueArray;
import com.jinphy.simplechatserver.models.network_models.CommonSession;
import com.jinphy.simplechatserver.models.network_models.PushSession;
import com.jinphy.simplechatserver.models.network_models.Response;
import com.jinphy.simplechatserver.network.controller.BaseController;
import com.jinphy.simplechatserver.utils.EncryptUtils;
import com.jinphy.simplechatserver.utils.GsonUtils;
import com.jinphy.simplechatserver.utils.NoUtils;
import com.jinphy.simplechatserver.utils.StringUtils;
import com.sun.org.apache.xpath.internal.operations.Bool;
import sun.security.util.Length;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.jinphy.simplechatserver.constants.StringConst.LINE;
import static com.jinphy.simplechatserver.models.network_models.Response.*;

/**
 * DESC: 网络请求接口处理类，在这里声明并实现各个接口的调用方法
 * <p>
 * 方法声明规则：
 * 1、 public
 * 2、 static
 * 3、 注解Path
 * 4、 只有一个Session 类型的参数
 * Created by jinphy on 2018/1/2.
 */
public class Api {

    //================================================================================\\
    //*********************接口调用方法  ***********************************************\\

    /**
     * DESC: 登录请求
     * Created by jinphy, on 2017/12/5, at 21:54
     */
    @Path(path = RequestConfig.Path.login)
    public static void login(CommonSession session) {
        String code;
        String msg;
        String account = session.params().get(RequestConfig.Key.account);
        String password = session.params().get(RequestConfig.Key.password);
        String deviceId = session.params().get(RequestConfig.Key.deviceId);
        Result result = null;
        if (StringUtils.isTrimEmpty(account)) {
            code = NO_PARAMS_MISSING;
            msg = "参数不完整！";
        } else {
            // 查询数据库
            result = UserDao.getInstance().login(account, password, deviceId);
            session.loggoer.append(result.logger + LINE);
            if (result.count < 0) {
                code = NO_SERVER;
                msg = "服务器异常，请稍后再试！";
            } else if (result.count == 0) {
                code = NO_FIND_USER;
                msg = "账号" + account + "不存在,\n请重新输入！";
            } else {
                if (result.data != null) {
                    code = YES;
                    msg = "登录成功！";

                    // 保存添加好友信息到数据库以让推送服务将该消息推送给对应的用户
                    Message message = new Message();
                    message.setContentType(Message.TYPE_SYSTEM_NOTICE);
                    message.setCreateTime(System.currentTimeMillis() + "");
                    message.setToAccount(account);
                    message.setExtra("简聊团队");
                    message.setContent("欢迎回来，您的信赖是我们最大的鼓励！");
                    MessageDao.getInstance().saveMessage(message);

                    BaseController.threadPools.execute(() -> {
                        try {
                            Thread.sleep(3000);
                            PushSession.pushMessage(account);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    code = NO_LOGIN;
                    msg = "密码错误，请重新输入！";
                }
            }
        }


        Response response = Response.make(code, msg, result == null ? null : result.first);
        session.server().broadcast(response.toString(), session.client());
        session.loggoer.append("response json: " + GsonUtils.toJson(response) + LINE)
                .append("=================网络请求结束=====================================================================\n\n");
        System.out.println(session.loggoer);

    }


    /**
     * DESC: 查询用户是否存在请求
     * Created by jinphy, on 2017/12/5, at 21:54
     */
    @Path(path = RequestConfig.Path.findUser)
    public static void findUser(CommonSession session) {
        String code;
        String msg;
        String account = session.params().get(RequestConfig.Key.account);
        Map<String, String> data = null;
        if (StringUtils.isTrimEmpty(account)) {
            code = NO_PARAMS_MISSING;
            msg = "参数不完整！";
        } else {
            Result result = UserDao.getInstance().findUser(account, User.ACCOUNT, User.NAME, User.AVATAR, User.SEX, User.ADDRESS);
            session.loggoer.append(result.logger + LINE);
            if (result.count < 0) {
                code = NO_SERVER;
                msg = "服务器异常，请稍后再试！";
            } else if (result.count == 0) {
                code = NO_FIND_USER;
                msg = "账号" + account + "不存在,\n请重新输入！";
            } else {
                code = YES;
                msg = "账号" + account + "存在";
                data = result.first;
            }
        }
        Response response = Response.make(code, msg, data);
        session.server().broadcast(response.toString(), session.client());
        session.loggoer.append("response json: " + GsonUtils.toJson(response) + LINE)
                .append("=================网络请求结束=====================================================================\n\n");
        System.out.println(session.loggoer);
    }


    /**
     * DESC: 创建新用户请求
     * Created by jinphy, on 2017/12/5, at 21:54
     */
    @Path(path = RequestConfig.Path.signUp)
    public static void createNewUser(CommonSession session) {
        String code;
        String msg;

        String account = session.params().get(RequestConfig.Key.account);
        String password = session.params().get(RequestConfig.Key.password);
        String date = session.params().get(RequestConfig.Key.date);
        if (StringUtils.isTrimEmpty(account, password, date)) {
            code = NO_PARAMS_MISSING;
            msg = "参数不完整！";
        } else {
            Result result = UserDao.getInstance().createNewUser(account, password, date);
            session.loggoer.append(result.logger + LINE);
            if (result.count < 0) {
                code = NO_SERVER;
                msg = "服务器异常，请稍后再试！";
            } else if (result.count == 0) {
                code = NO_PARAMS_ERROR;
                msg = "请求参数错误，请重新检查";
            } else {
                code = YES;
                msg = "恭喜您，账号注册成功！";

                // 注册成功，添加默认好友
                Friend.addDefault(account);

                // 保存添加好友信息到数据库以让推送服务将该消息推送给对应的用户
                Message message = new Message();
                message.setContentType(Message.TYPE_SYSTEM_NOTICE);
                message.setCreateTime(System.currentTimeMillis() + "");
                message.setToAccount(account);
                message.setExtra("简聊团队");
                message.setContent("恭喜您成为简聊的一员，让我们开始新的旅程，和我们一起成长吧！");
                MessageDao.getInstance().saveMessage(message);
            }
        }
        Response response = Response.make(code, msg, null);
        session.server().broadcast(response.toString(), session.client());
        session.loggoer.append("response json: " + GsonUtils.toJson(response) + LINE)
                .append("=================网络请求结束=====================================================================\n\n");
        System.out.println(session.loggoer);
    }


    @Path(path = RequestConfig.Path.modifyUserInfo)
    public static void modifyUserInfo(CommonSession session) {
        String code;
        String msg;
        Map<String, String> data = null;

        String account = session.params().remove(RequestConfig.Key.account);
        String accessToken = session.params().remove(RequestConfig.Key.accessToken);
        String deviceId = session.params().remove(RequestConfig.Key.deviceId);
        System.out.println("account = " + account);
        System.out.println("accessToken = " + accessToken);
        System.out.println("deviceId = " + deviceId);
        if (StringUtils.isTrimEmpty(account, deviceId, accessToken)) {
            code = NO_PARAMS_MISSING;
            msg = "参数不完整！";
        } else {
            Result findResult = UserDao.getInstance().findUser(account);
            String check = AccessToken.check(findResult.first.get(User.ACCESS_TOKEN), accessToken);
            if (check != AccessToken.OK) {
                // 令牌过期
                code = NO_ACCESS_TOKEN;
                msg = check;
            } else {
                // 参数正确

                // 更新AccessToken
                session.params().put(User.ACCESS_TOKEN, AccessToken.make(deviceId, User.STATUS_LOGIN).toString());

                // 解析参数
                KeyValueArray array = KeyValueArray.parse(session.params());

                // 更新数据库，更新时account和deviceId要去除
                Result result = UserDao.getInstance().updateUser(account, array.keys, array.values);

                session.loggoer.append(result.logger + LINE);
                if (result.count < 0) {
                    code = NO_SERVER;
                    msg = "服务器异常，请稍后再试！";
                } else if (result.count == 0) {
                    code = NO_PARAMS_ERROR;
                    msg = "请求参数错误，请重新检查";
                } else {
                    code = YES;
                    msg = "个人信息修改成功！";
                    // 更新成功后要把account加回来，并且把password删除
                    // 所以最终返回给客户端的参数比入参少了deviceId和password两个字段
                    session.params().put(User.ACCOUNT, account);
                    session.params().remove(User.PASSWORD);
                    data = session.params();

                    // 通知所有好友更新账号信息
                    UserDao.getInstance().notifyFriends(account, FriendDao.getInstance().getAllFriendAccount(account));
                }
            }
        }
        Response response = Response.make(code, msg, data);
        session.server().broadcast(response.toString(), session.client());
        session.loggoer.append("response json: " + GsonUtils.toJson(response) + LINE)
                .append("=================网络请求结束=====================================================================\n\n");
        System.out.println(session.loggoer);
    }

    @Path(path = RequestConfig.Path.logout)
    public static void logout(CommonSession session) {
        String code;
        String msg;
        Map<String, String> data = null;

        String account = session.params().remove(RequestConfig.Key.account);
        String accessToken = session.params().remove(RequestConfig.Key.accessToken);
        if (StringUtils.isTrimEmpty(account, accessToken)) {
            code = NO_PARAMS_MISSING;
            msg = "参数不完整！";
        } else {
            Result findResult = UserDao.getInstance().findUser(account);
            String check = AccessToken.check(findResult.first.get(User.ACCESS_TOKEN), accessToken);
            if (check != AccessToken.OK) {
                // 令牌过期
                code = NO_ACCESS_TOKEN;
                msg = check;
            } else {
                // 参数正确

                // 更新AccessToken
                AccessToken updatedAccessToken = AccessToken.parse(accessToken);
                updatedAccessToken.setStatus(User.STATUS_LOGOUT);
                session.params().put(User.ACCESS_TOKEN, updatedAccessToken.toString());
                session.params().put(User.STATUS, User.STATUS_LOGOUT);

                // 解析参数
                KeyValueArray array = KeyValueArray.parse(session.params());

                // 更新数据库，更新时account和deviceId要去除
                Result result = UserDao.getInstance().updateUser(account, array.keys, array.values);

                session.loggoer.append(result.logger + LINE);
                if (result.count < 0) {
                    code = NO_SERVER;
                    msg = "服务器异常，请稍后再试！";
                } else if (result.count == 0) {
                    code = NO_PARAMS_ERROR;
                    msg = "请求参数错误，请重新检查";
                } else {
                    code = YES;
                    msg = "退出登录成功！";
                    // 更新成功后要把account加回来，并且把登录状态修改为登出状态
                    session.params().put(User.ACCOUNT, account);
                    data = session.params();
                }
            }
        }
        Response response = Response.make(code, msg, data);
        session.server().broadcast(response.toString(), session.client());
        session.loggoer.append("response json: " + GsonUtils.toJson(response) + LINE)
                .append("=================网络请求结束=====================================================================\n\n");
        System.out.println(session.loggoer);
    }


    /**
     * DESC: 添加好友接口
     * Created by jinphy, on 2018/2/27, at 9:37
     */
    @Path(path = RequestConfig.Path.addFriend)
    public static void addFriend(CommonSession session) {
        String code = null;
        String msg = null;
        // 获取参数
        String requestAccount = session.params().get(RequestConfig.Key.requestAccount);
        String receiveAccount = session.params().get(RequestConfig.Key.receiveAccount);
        String remark = session.params().get(RequestConfig.Key.remark);
        String verifyMsg = session.params().get(RequestConfig.Key.verifyMsg);
        String confirm = session.params().get(RequestConfig.Key.confirm);
        String date = session.params().get(RequestConfig.Key.date);

        FriendDao friendDao = FriendDao.getInstance();
        if (confirm == null) {
            // 申请成为好友

            // 建立好友关系
            friendDao.addFriend(requestAccount, receiveAccount);
            friendDao.addFriend(receiveAccount, requestAccount);
            if (StringUtils.isNonEmpty(remark)) {
                friendDao.modifyRemark(receiveAccount, requestAccount, remark);
            }

            // 保存添加好友信息到数据库以让推送服务将该消息推送给对应的用户
            Message message = new Message();
            message.setContentType(Message.TYPE_SYSTEM_ADD_FRIEND);
            message.setCreateTime(System.currentTimeMillis() + "");
            message.setToAccount(receiveAccount);
            message.setExtra(requestAccount);
            message.setContent(StringUtils.isNonEmpty(verifyMsg) ? verifyMsg : "");
            MessageDao.getInstance().saveMessage(message);

            code = YES;
            msg = "添加好友申请已发送！";
        } else {
            // 回复好友申请
            if ("1".equals(confirm)) {
                // 同意好友申请
                friendDao.modifyStatus(requestAccount, receiveAccount, Friend.STATUS_OK);
                friendDao.modifyStatus(receiveAccount, requestAccount, Friend.STATUS_OK);
                if (StringUtils.isNonEmpty(remark)) {
                    friendDao.modifyRemark(receiveAccount, requestAccount, remark);
                }
                friendDao.setDate(requestAccount, receiveAccount, date);
                friendDao.setDate(receiveAccount, requestAccount, date);

                // 保存添加好友信息到数据库以让推送服务将该消息推送给对应的用户
                Message message = new Message();
                message.setContentType(Message.TYPE_SYSTEM_ADD_FRIEND_AGREE);
                message.setCreateTime(System.currentTimeMillis() + "");
                message.setToAccount(receiveAccount);
                message.setContent("好友申请已同意！");
                message.setExtra(requestAccount);
                MessageDao.getInstance().saveMessage(message);

                code = Response.YES;
                msg = "同意好友申请成功！";
            } else {
                // 拒绝好友申请
                friendDao.deleteFriend(requestAccount, receiveAccount);
                friendDao.deleteFriend(receiveAccount, requestAccount);
                code = Response.YES;
                msg = "拒绝好友申请成功！";
            }


        }
        Response response = Response.make(code, msg, null);
        session.server().broadcast(response.toString(), session.client());
        session.loggoer.append("response json: " + GsonUtils.toJson(response) + LINE)
                .append("=================网络请求结束=====================================================================\n\n");
        System.out.println(session.loggoer);

    }

    /**
     * DESC: 加载指定用户对应的所有好友
     * Created by jinphy, on 2018/2/28, at 17:45
     */
    @Path(path = RequestConfig.Path.loadFriends)
    public static void loadFriends(CommonSession session) {
        String code;
        String msg;
        List<Map<String, String>> friends = null;

        String owner = session.params().get(Friend.OWNER);
        if (StringUtils.isTrimEmpty(owner)) {
            code = Response.NO_PARAMS_MISSING;
            msg = "参数不完整！";
        } else {
            Result result = FriendDao.getInstance().loadFriends(owner);
            // 任何账号默认都有一个“系统消息”好友
            if (result.count < 0) {
                code = Response.NO_PARAMS_ERROR;
                msg = "请求参数错误，请重新检查";
            } else {
                code = Response.YES;
                msg = "加载好友成功！";
                friends = result.data;
            }
        }

        Response response = Response.make(code, msg, friends);
        session.server().broadcast(response.toString(), session.client());
        session.loggoer.append("response json: " + GsonUtils.toJson(response) + LINE)
                .append("=================网络请求结束=====================================================================\n\n");
        System.out.println(session.loggoer);
    }

    /**
     * DESC: 获取指定的好友
     * Created by jinphy, on 2018/3/2, at 9:28
     */
    @Path(path = RequestConfig.Path.getFriend)
    public static void getFriend(CommonSession session) {
        String code;
        String msg;
        Map<String, String> friend = null;

        String owner = session.params().get(Friend.OWNER);
        String account = session.params().get(Friend.ACCOUNT);
        if (StringUtils.isTrimEmpty(owner) || StringUtils.isTrimEmpty(account)) {
            code = Response.NO_PARAMS_MISSING;
            msg = "参数不完整！";
        } else {
            Result result = FriendDao.getInstance().getFriend(owner, account);
            // 任何账号默认都有一个“系统消息”好友
            if (result.count <= 0) {
                code = Response.NO_PARAMS_ERROR;
                msg = "请求参数错误，请重新检查";
            } else {
                code = Response.YES;
                msg = "获取好友成功！";
                friend = result.first;
            }
        }

        Response response = Response.make(code, msg, friend);
        session.server().broadcast(response.toString(), session.client());
        session.loggoer.append("response json: " + GsonUtils.toJson(response) + LINE)
                .append("=================网络请求结束=====================================================================\n\n");
        System.out.println(session.loggoer);
    }


    /**
     * DESC: 获取指定账号头像接口
     * Created by jinphy, on 2018/3/1, at 9:05
     */
    @Path(path = RequestConfig.Path.getAvatar)
    public static void getAvatar(CommonSession session) {
        String code;
        String msg;
        Map<String, String> avatar = null;

        String account = session.params().get(User.ACCOUNT);
        if (StringUtils.isTrimEmpty(account)) {
            code = Response.NO_PARAMS_MISSING;
            msg = "参数不完整！";
        } else {
            Result result;
            if (account.contains("G")) {
                // 群头像
                result = GroupDao.getInstance().getAvatar(account);
            } else {
                // 好友头像
                result = FriendDao.getInstance().getAvatar(account);
            }
            // 任何账号默认都有一个“系统消息”好友
            if (result.count <= 0) {
                code = Response.NO_PARAMS_ERROR;
                msg = "请求参数错误，请重新检查";
            } else {
                code = Response.YES;
                msg = "头像加载成功！";
                avatar = result.first;
            }
        }

        Response response = Response.make(code, msg, avatar);
        session.server().broadcast(response.toString(), session.client());
        session.loggoer.append("response json: " + GsonUtils.toJson(response) + LINE)
                .append("=================网络请求结束=====================================================================\n\n");
        System.out.println(session.loggoer);
    }

    /**
     * DESC: 获取头像（多个）
     * Created by jinphy, on 2018/3/11, at 17:02
     */
    @Path(path = RequestConfig.Path.loadAvatars)
    public static void loadAvatars(CommonSession session) {
        String code;
        String msg;
        List<Map<String, String>> avatars = null;

        String accountsStr = session.params().get(RequestConfig.Key.accounts);

        if (StringUtils.isTrimEmpty(accountsStr)) {
            code = Response.NO_PARAMS_MISSING;
            msg = "参数不完整！";
        } else {
            Result result;
            Result friendResult;
            Result groupResult;
            String[] accounts = GsonUtils.toBean(accountsStr, String[].class);


            // 群头像
            groupResult = GroupDao.getInstance().loadAvatars(accounts);
            // 好友头像
            friendResult = FriendDao.getInstance().loadAvatars(accounts);

            int count = 0;
            if (groupResult.count > 0) {
                count += groupResult.count;
            }
            if (friendResult.count > 0) {
                count += friendResult.count;
            }

            result = Result.ok(
                    count,
                    new LinkedList<Map<String, String>>(),
                    null);
            if (groupResult.count > 0) {
                for (Map<String, String> map : groupResult.data) {
                    result.data.add(map);
                }
            }
            if (friendResult.count > 0) {
                for (Map<String, String> map : friendResult.data) {
                    result.data.add(map);
                }
            }

            if (result.count <= 0) {
                code = Response.NO_PARAMS_ERROR;
                msg = "请求参数错误，请重新检查";
            } else {
                code = Response.YES;
                msg = "头像加载成功！";
                avatars = result.data;
            }
            System.out.println("friendAvatars: " + friendResult.count);
            System.out.println("groupAvatars: " + groupResult.count);
        }

        Response response = Response.make(code, msg, avatars);
        session.server().broadcast(response.toString(), session.client());
        session.loggoer.append("response json: " + GsonUtils.toJson(response) + LINE)
                .append("=================网络请求结束=====================================================================\n\n");
        System.out.println(session.loggoer);
    }

    /**
     * DESC: 修改好友信息接口
     * Created by jinphy, on 2018/3/2, at 18:27
     */
    @Path(path = RequestConfig.Path.modifyRemark)
    public static void modifyRemark(CommonSession session) {
        String code;
        String msg;

        String account = session.params().get(Friend.ACCOUNT);
        String owner = session.params().get(Friend.OWNER);
        String remark = session.params().get(Friend.REMARK);
        if (StringUtils.isTrimEmpty(account)
                || StringUtils.isTrimEmpty(owner)
                || StringUtils.isTrimEmpty(remark)) {
            code = Response.NO_PARAMS_MISSING;
            msg = "参数不完整！";
        } else {
            FriendDao friendDao = FriendDao.getInstance();
            Result result = friendDao.modifyRemark(account, owner, remark);
            // 任何账号默认都有一个“系统消息”好友
            if (result.count <= 0) {
                code = Response.NO_PARAMS_ERROR;
                msg = "请求参数错误，请重新检查";
            } else {
                code = Response.YES;
                msg = "修改好友备注成功！";
            }
        }

        Response response = Response.make(code, msg, null);
        session.server().broadcast(response.toString(), session.client());
        session.loggoer.append("response json: " + GsonUtils.toJson(response) + LINE)
                .append("=================网络请求结束=====================================================================\n\n");
        System.out.println(session.loggoer);
    }


    /**
     * DESC: 修改好友信息接口
     * Created by jinphy, on 2018/3/2, at 18:27
     */
    @Path(path = RequestConfig.Path.modifyStatus)
    public static void modifyStatus(CommonSession session) {
        String code;
        String msg;

        String account = session.params().get(Friend.ACCOUNT);
        String owner = session.params().get(Friend.OWNER);
        String status = session.params().get(Friend.STATUS);
        if (StringUtils.isTrimEmpty(account)
                || StringUtils.isTrimEmpty(owner)
                || StringUtils.isTrimEmpty(status)) {
            code = Response.NO_PARAMS_MISSING;
            msg = "参数不完整！";
        } else {
            FriendDao friendDao = FriendDao.getInstance();
            Result result = null;
            if (Friend.STATUS_BLACK_LISTING.equals(status)) {
                result = friendDao.modifyStatus(account, owner, Friend.STATUS_BLACK_LISTING);
                result = friendDao.modifyStatus(owner, account, Friend.STATUS_BLACK_LISTED);
            } else if (Friend.STATUS_OK.equals(status)) {
                result = friendDao.modifyStatus(account, owner, status);
                result = friendDao.modifyStatus(owner, account, status);
            }

            // 任何账号默认都有一个“系统消息”好友
            if (result.count <= 0) {
                code = Response.NO_PARAMS_ERROR;
                msg = "请求参数错误，请重新检查";
            } else {
                code = Response.YES;
                msg = "修改好友状态成功！";

                // 保存添加好友信息到数据库以让推送服务将该消息推送给对应的用户
                Message message = new Message();
                message.setContentType(Message.TYPE_SYSTEM_RELOAD_FRIEND);
                message.setCreateTime(System.currentTimeMillis() + "");
                message.setToAccount(account);
                message.setExtra(owner);
                message.setContent("重新加载好友");
                MessageDao.getInstance().saveMessage(message);
            }
        }

        Response response = Response.make(code, msg, null);
        session.server().broadcast(response.toString(), session.client());
        session.loggoer.append("response json: " + GsonUtils.toJson(response) + LINE)
                .append("=================网络请求结束=====================================================================\n\n");
        System.out.println(session.loggoer);
    }

    @Path(path = RequestConfig.Path.deleteFriend)
    public static void deleteFriend(CommonSession session) {
        String code;
        String msg;

        String account = session.params().get(Friend.ACCOUNT);
        String owner = session.params().get(Friend.OWNER);
        if (StringUtils.isTrimEmpty(account)
                || StringUtils.isTrimEmpty(owner)) {
            code = Response.NO_PARAMS_MISSING;
            msg = "参数不完整！";
        } else {
            FriendDao friendDao = FriendDao.getInstance();
            Result result = friendDao.deleteFriend(account, owner);
            // 任何账号默认都有一个“系统消息”好友
            if (result.count <= 0) {
                code = Response.NO_PARAMS_ERROR;
                msg = "请求参数错误，请重新检查";
            } else {
                code = Response.YES;
                msg = "删除好友成功！";

                // 保存添加好友信息到数据库以让推送服务将该消息推送给对应的用户
                Message message = new Message();
                message.setContentType(Message.TYPE_SYSTEM_DELETE_FRIEND);
                message.setCreateTime(System.currentTimeMillis() + "");
                message.setToAccount(account);
                message.setExtra(owner);
                message.setContent("已经删除了好友关系，你们现在不是好友了！");
                MessageDao.getInstance().saveMessage(message);
            }
        }

        Response response = Response.make(code, msg, null);
        session.server().broadcast(response.toString(), session.client());
        session.loggoer.append("response json: " + GsonUtils.toJson(response) + LINE)
                .append("=================网络请求结束=====================================================================\n\n");
        System.out.println(session.loggoer);
    }

    @Path(path = RequestConfig.Path.checkAccount)
    public static void checkAccount(CommonSession session) {
        String code;
        String msg;
        Map<String, String> data = null;

        String account = session.params().remove(RequestConfig.Key.account);
        String accessToken = session.params().remove(RequestConfig.Key.accessToken);
        if (StringUtils.isTrimEmpty(account, accessToken)) {
            code = NO_PARAMS_MISSING;
            msg = "参数不完整！";
        } else {
            Result findResult = UserDao.getInstance().findUser(account);
            String check = AccessToken.check(findResult.first.get(User.ACCESS_TOKEN), accessToken);
            if (check != AccessToken.OK) {
                // 令牌过期
                code = NO_ACCESS_TOKEN;
                msg = check;
            } else {
                // 参数正确

                // 更新AccessToken
                AccessToken oldAccessToken = AccessToken.parse(accessToken);
                AccessToken newAccessToken = AccessToken.make(oldAccessToken.getDeviceId(), oldAccessToken.getStatus());
                accessToken = newAccessToken.toString();
                session.params().put(User.ACCESS_TOKEN, accessToken);

                // 解析参数
                KeyValueArray array = KeyValueArray.parse(session.params());

                Result result = UserDao.getInstance().updateUser(account, array.keys, array.values);

                session.loggoer.append(result.logger + LINE);
                if (result.count < 0) {
                    code = NO_SERVER;
                    msg = "服务器异常，请稍后再试！";
                } else if (result.count == 0) {
                    code = NO_PARAMS_ERROR;
                    msg = "请求参数错误，请重新检查";
                } else {
                    code = YES;
                    msg = "账号令牌更新成功！";
                    // 更新成功后要把account加回来
                    session.params().put(User.ACCOUNT, account);
                    data = session.params();
                }
            }
        }
        Response response = Response.make(code, msg, data);
        session.server().broadcast(response.toString(), session.client());
        session.loggoer.append("response json: " + GsonUtils.toJson(response) + LINE)
                .append("=================网络请求结束=====================================================================\n\n");
        System.out.println(session.loggoer);
    }

    /**
     * DESC: 创建新群聊
     * Created by jinphy, on 2018/3/10, at 14:26
     */
    @Path(path = RequestConfig.Path.createGroup)
    public static void createGroup(CommonSession session) {
        String code;
        String msg;
        Map<String, String> data = null;

        String accessToken = session.params().remove(RequestConfig.Key.accessToken);
        String members = session.params().remove(RequestConfig.Key.members);

        String name = session.params().get(RequestConfig.Key.name);
        String autoAdd = session.params().get(RequestConfig.Key.autoAdd);
        String maxCount = session.params().get(RequestConfig.Key.maxCount);
        String avatar = session.params().get(RequestConfig.Key.avatar);
        String creator = session.params().get(RequestConfig.Key.creator);
        if (StringUtils.isTrimEmpty(accessToken, name, autoAdd, maxCount, members, creator)) {
            code = NO_PARAMS_MISSING;
            msg = "参数不完整！";
        } else {
            Result findResult = UserDao.getInstance().findUser(creator);
            String check = AccessToken.check(findResult.first.get(User.ACCESS_TOKEN), accessToken);
            if (check != AccessToken.OK) {
                // 令牌过期
                code = NO_ACCESS_TOKEN;
                msg = check;
            } else {
                // 参数正确
                session.params().put(RequestConfig.Key.groupNo, NoUtils.generateGroupNo());
                session.params().put(RequestConfig.Key.showMemberName, "true");
                session.params().put(RequestConfig.Key.keepSilent, "false");
                session.params().put(RequestConfig.Key.rejectMsg, "false");

                String[] memberArray = GsonUtils.toBean(members, String[].class);

                // 创建群和对应的成员
                boolean b = GroupDao.getInstance().buildGroup(session.params(), memberArray);

                if (!b) {
                    code = NO_SERVER;
                    msg = "服务器异常，请稍后再试！";
                } else {

                    String content = new String("我创建了群聊，我们开始聊天吧！");
                    // 建立好友关系后，通知各个成员更新好友、群、群成员
                    // 保存添加好友信息到数据库以让推送服务将该消息推送给对应的用户
                    Message message = new Message();
                    message.setContentType(Message.TYPE_SYSTEM_NEW_GROUP);
                    message.setCreateTime(System.currentTimeMillis() + "");
                    message.setFromAccount(Friend.system);
                    String groupNo = session.params().get(RequestConfig.Key.groupNo);
                    String membersStr = GsonUtils.toJson(memberArray);
                    message.setExtra(groupNo + "@" + membersStr);
                    message.setContent(content);
                    for (String member : memberArray) {
                        if (StringUtils.noEqual(creator, member)) {
                            message.setToAccount(member);
                            MessageDao.getInstance().saveMessage(message);
                        }
                    }
                    data = GroupDao.getInstance().get(session.params().get(RequestConfig.Key.groupNo), creator).first;
                    data.put("msg", content);
                    code = YES;
                    msg = "群聊创建成功！";
                }
            }
        }

        Response response = Response.make(code, msg, data);
        session.server().broadcast(response.toString(), session.client());
        session.loggoer.append("response json: " + GsonUtils.toJson(response) + LINE)
                .append("=================网络请求结束=====================================================================\n\n");
        System.out.println(session.loggoer);
    }

    @Path(path = RequestConfig.Path.getGroups)
    public static void getGroups(CommonSession session) {
        String code;
        String msg;
        Response response = null;

        String groupNo = session.params().remove(RequestConfig.Key.groupNo);
        String owner = session.params().remove(RequestConfig.Key.owner);
        String text = session.params().remove(RequestConfig.Key.text);
        if (StringUtils.isEmpty(text) && StringUtils.isTrimEmpty(owner)) {
            code = NO_PARAMS_MISSING;
            msg = "参数不完整！";
        } else {
            Result result = null;
            if (StringUtils.isEmpty(text)) {
                result = GroupDao.getInstance().get(groupNo, owner);
            } else {
                result = GroupDao.getInstance().search(text);
            }
            session.loggoer.append(result.logger + LINE);
            if (result.count < 0) {
                code = NO_SERVER;
                msg = "服务器异常，请稍后再试！";
            } else if (!StringUtils.isEmpty(text)) {
                // 搜索查询
                if (result.count == 0) {
                    code = NO_EMPTY_RESULT;
                    msg = "没有找到相应的群！";
                } else {
                    code = YES;
                    msg = "查找群成功！";
                    response = Response.make(code, msg, result.data);
                }
            } else if (!StringUtils.isEmpty(groupNo)) {
                // 查询单条数据
                if (result.count == 0) {
                    code = NO_PARAMS_ERROR;
                    msg = "请求参数错误，请重新检查";
                } else {
                    code = YES;
                    msg = "获取群聊成功！";
                    response = Response.make(code, msg, result.first);
                }
            } else {
                code = YES;
                msg = "获取群聊成功！";
                response = Response.make(code, msg, result.data);
            }
        }
        if (response == null) {
            response = Response.make(code, msg, null);
        }
        session.server().broadcast(response.toString(), session.client());
        session.loggoer.append("response json: " + GsonUtils.toJson(response) + LINE)
                .append("=================网络请求结束=====================================================================\n\n");
        System.out.println(session.loggoer);
    }

    @Path(path = RequestConfig.Path.modifyGroup)
    public static void modifyGroup(CommonSession session) {

        Response response = GroupDao.getInstance().modifyGroup(session.params());

        session.server().broadcast(response.toString(), session.client());
        session.loggoer.append("response json: " + GsonUtils.toJson(response) + LINE)
                .append("=================网络请求结束=====================================================================\n\n");
        System.out.println(session.loggoer);
    }

    @Path(path = RequestConfig.Path.joinGroup)
    public static void joinGroup(CommonSession session) {
        String code;
        String msg;

        String groupNo = session.params().get(Group.GROUP_NO);
        String creator = session.params().get(Group.CREATOR);
        String account = session.params().get(User.ACCOUNT);
        String extraMsg = session.params().getOrDefault(RequestConfig.Key.extraMsg,"无");
        if (StringUtils.isEmpty(groupNo, creator, account)) {
            code = NO_PARAMS_MISSING;
            msg = "参数不完整！";
        } else {
            // 查询要加入的群
            Result groups = GroupDao.getInstance().get(groupNo);

            session.loggoer.append(groups.logger + LINE);
            if (groups.count < 0) {
                code = NO_SERVER;
                msg = "服务器异常，请稍后再试！";
            } else if (groups.count == 0) {
                code = NO_EMPTY_RESULT;
                msg = "没有找到相应的群！";
            } else {
                Integer maxCount = Integer.valueOf(groups.first.get(Group.MAX_COUNT));
                if (maxCount <= groups.count) {
                    code = NO_GROUP_FULL;
                    msg = "该群人数已满！";
                } else {
                    Result memberResult = MemberDao.getInstance().getMember(groupNo, account);
                    if (memberResult.count > 0) {
                        code = NO_MEMBER_EXIT;
                        msg = "成员成员已存在!";
                    } else {
                        boolean autoAdd = Boolean.valueOf(groups.first.get(Group.AUTO_ADD));
                        if (autoAdd) {
                            Response response = GroupDao.getInstance().joinGroup(groups.first, account);
                            code = response.getCode();
                            msg = response.getMsg();
                        } else {
                            // 成员加入需要群主验证
                            Message message = new Message();
                            message.setCreateTime(System.currentTimeMillis() + "");
                            message.setFromAccount(Friend.system);
                            message.setToAccount(creator);
                            message.setContentType(Message.TYPE_SYSTEM_APPLY_JOIN_GROUP);
                            message.setContent("新成员入群申请！");
                            message.setExtra(groupNo + "@" + account + "@" + Group.STATUS_WAITING + "@" + EncryptUtils.aesEncrypt(extraMsg));
                            MessageDao messageDao = MessageDao.getInstance();
                            messageDao.saveMessage(message);
                            code = YES;
                            msg = "申请已发送，请等待群主通过！";
                        }
                    }
                }
            }
        }

        Response response = Response.make(code, msg, null);
        session.server().broadcast(response.toString(), session.client());
        session.loggoer.append("response json: " + GsonUtils.toJson(response) + LINE)
                .append("=================网络请求结束=====================================================================\n\n");
        System.out.println(session.loggoer);
    }


    @Path(path = RequestConfig.Path.addMembers)
    public static void addMembers(CommonSession session) {
        String code;
        String msg;

        String groupNo = session.params().get(RequestConfig.Key.groupNo);
        String accountsStr = session.params().get(RequestConfig.Key.accounts);
        String operator = session.params().get(RequestConfig.Key.operator);
        if (StringUtils.isEmpty(groupNo, accountsStr, operator)) {
            code = NO_PARAMS_MISSING;
            msg = "参数不完整！";
        } else {
            // 查询要加入的群
            Result groups = GroupDao.getInstance().get(groupNo);

            session.loggoer.append(groups.logger + LINE);
            if (groups.count < 0) {
                code = NO_SERVER;
                msg = "服务器异常，请稍后再试！";
            } else if (groups.count == 0) {
                code = NO_EMPTY_RESULT;
                msg = "没有找到相应的群！";
            } else {
                String[] accounts = GsonUtils.toBean(accountsStr, String[].class);

                if (accounts == null || accounts.length == 0) {
                    code = NO_PARAMS_MISSING;
                    msg = "参数不完整！";
                } else {
                    Integer maxCount = Integer.valueOf(groups.first.get(Group.MAX_COUNT));
                    if (maxCount <= groups.count) {
                        code = NO_GROUP_FULL;
                        msg = "该群人数已满！";
                    } else if (maxCount < groups.count + accounts.length) {
                        code = NO_GROUP_FULL;
                        msg = new String("该群还可加入" + (maxCount - groups.count) + "，小于要添加的成员数量");
                    }else {
                        boolean result = GroupDao.getInstance().addMembers(groups.first, operator, accounts);
                        if (result) {
                            code = YES;
                            msg = "成员添加成功！";

                            String groupName = groups.first.get(Group.NAME);

                            MessageDao messageDao = MessageDao.getInstance();
                            Message message = new Message();
                            message.setCreateTime(System.currentTimeMillis() + "");
                            message.setFromAccount(Friend.system);

                            // 通知新加入群聊的成员加载群聊
                            message.setContentType(Message.TYPE_SYSTEM_NEW_GROUP);
                            List<String> memberList = MemberDao.getInstance().getMemberAccounts(groupNo);
                            String membersStr = GsonUtils.toJson(memberList);
                            message.setExtra(groupNo + "@" + membersStr);
                            message.setContent(new String(operator + "把你加入群聊：" + groupName + "(" + groupNo + ")"));
                            for (String account : accounts) {
                                message.setToAccount(account);
                                messageDao.saveMessage(message);
                            }

                            // 通知以存在的成员加载新成员（除了操作者外）
                            message.setContentType(Message.TYPE_SYSTEM_NEW_MEMBER);
                            message.setContent(groupNo);
                            message.setExtra(GsonUtils.toJson(accounts));

                            String[] temp = new String[accounts.length + 1];
                            for (int i = 0; i < accounts.length; i++) {
                                temp[i] = accounts[i];
                            }
                            temp[accounts.length] = operator;
                            List<String> existMembers = MemberDao.getInstance().getMemberAccounts(groupNo, temp);
                            if (existMembers != null && existMembers.size() > 0) {
                                for (String member : existMembers) {
                                    message.setToAccount(member);
                                    messageDao.saveMessage(message);
                                }
                            }
                        } else {
                            code = NO_SERVER;
                            msg = "服务器异常，请稍后再试！";
                        }
                    }

                }
            }
        }

        Response response = Response.make(code, msg, null);
        session.server().broadcast(response.toString(), session.client());
        session.loggoer.append("response json: " + GsonUtils.toJson(response) + LINE)
                .append("=================网络请求结束=====================================================================\n\n");
        System.out.println(session.loggoer);
    }

    @Path(path = RequestConfig.Path.agreeJoinGroup)
    public static void agreeJoinGroup(CommonSession session) {
        String code;
        String msg;

        String groupNo = session.params().remove(Group.GROUP_NO);
        String creator = session.params().remove(Group.CREATOR);
        String account = session.params().remove(User.ACCOUNT);
        if (StringUtils.isEmpty(groupNo, creator, account)) {
            code = NO_PARAMS_MISSING;
            msg = "参数不完整！";
        } else {
            // 查询要加入的群
            Result groups = GroupDao.getInstance().get(groupNo);

            session.loggoer.append(groups.logger + LINE);
            if (groups.count < 0) {
                code = NO_SERVER;
                msg = "服务器异常，请稍后再试！";
            } else if (groups.count == 0) {
                code = NO_EMPTY_RESULT;
                msg = "没有找到相应的群！";
            } else {
                Integer maxCount = Integer.valueOf(groups.first.get(Group.MAX_COUNT));
                if (maxCount <= groups.count) {
                    code = NO_GROUP_FULL;
                    msg = "该群人数已满！";
                }else {
                    Result memberResult = MemberDao.getInstance().getMember(groupNo, account);
                    if (memberResult.count > 0) {
                        code = NO_MEMBER_EXIT;
                        msg = "成员已存在！";
                    } else {
                        Response response = GroupDao.getInstance().joinGroup(groups.first, account);
                        if (YES.equals(response.getCode())) {
                            code = YES;
                            msg = "您已通过" + account + "的申请！";
                        } else {
                            code = response.getCode();
                            msg = response.getMsg();
                        }
                    }
                }
            }
        }

        Response response = Response.make(code, msg, null);
        session.server().broadcast(response.toString(), session.client());
        session.loggoer.append("response json: " + GsonUtils.toJson(response) + LINE)
                .append("=================网络请求结束=====================================================================\n\n");
        System.out.println(session.loggoer);
    }

    @Path(path = RequestConfig.Path.exitGroup)
    public static void exitGroup(CommonSession session) {
        String code;
        String msg;
        Response response = null;

        String groupNo = session.params().get(RequestConfig.Key.groupNo);
        String account = session.params().get(RequestConfig.Key.account);
        String operator = session.params().get(RequestConfig.Key.operator);
        if (StringUtils.isEmpty(groupNo, operator)) {
            code = NO_PARAMS_MISSING;
            msg = "参数不完整！";
        } else {
            GroupDao groupDao = GroupDao.getInstance();
            Result groupResult = groupDao.get(groupNo, operator);

            if (groupResult.count < 0) {
                code = NO_SERVER;
                msg = "服务器异常，请稍后再试！";
            } else if (groupResult.count == 0) {
                code = NO_PARAMS_ERROR;
                msg = "请求参数错误，请重新检查";
            } else {
                Map<String, String> group = groupResult.first;
                String creator = group.get(Group.CREATOR);
                if (StringUtils.isEmpty(account)) {
                    // 解散群聊
                    boolean result = GroupDao.getInstance().breakGroup(groupNo, creator);
                    if (result) {
                        code = YES;
                        msg = "操作成功！";
                    } else {
                        code = NO_SERVER;
                        msg = "服务器异常，请稍后再试！";
                    }
                } else {
                    // 删除群员
                    Boolean result = null;
                    if (StringUtils.equal(operator, account)) {
                        // 是成员自己退出群聊
                        result = GroupDao.getInstance().removeMember(groupNo, creator, account, true);
                    } else if (StringUtils.equal(operator, creator)) {
                        // 是群主移除该成员account
                        result = GroupDao.getInstance().removeMember(groupNo, creator, account, false);
                    }
                    if (result == null) {
                        code = NO_PARAMS_ERROR;
                        msg = "请求参数错误，请重新检查";
                    } else if (!result) {
                        code = NO_SERVER;
                        msg = "服务器异常，请稍后再试！";
                    } else {
                        code = YES;
                        msg = "操作成功！";
                    }
                }

            }

        }
        response = Response.make(code, msg, null);
        session.server().broadcast(response.toString(), session.client());
        session.loggoer.append("response json: " + GsonUtils.toJson(response) + LINE)
                .append("=================网络请求结束=====================================================================\n\n");
        System.out.println(session.loggoer);
    }


    @Path(path = RequestConfig.Path.removeMembers)
    public static void removeMembers(CommonSession session) {
        String code;
        String msg;
        Response response = null;

        String groupNo = session.params().get(RequestConfig.Key.groupNo);
        String accountsStr = session.params().get(RequestConfig.Key.accounts);
        String operator = session.params().get(RequestConfig.Key.operator);
        if (StringUtils.isEmpty(groupNo, operator,accountsStr)) {
            code = NO_PARAMS_MISSING;
            msg = "参数不完整！";
        } else {
            GroupDao groupDao = GroupDao.getInstance();
            Result groupResult = groupDao.get(groupNo, operator);

            if (groupResult.count <0) {
                code = NO_SERVER;
                msg = "服务器异常，请稍后再试！";
            } else if (groupResult.count == 0) {
                code = NO_PARAMS_ERROR;
                msg = "请求参数错误，请重新检查";
            } else {
                Map<String, String> group = groupResult.first;
                String creator = group.get(Group.CREATOR);
                String[] accounts = GsonUtils.toBean(accountsStr, String[].class);

                if (accounts == null || accounts.length == 0) {
                    code = NO_PARAMS_MISSING;
                    msg = "参数不完整！";
                } else {
                    // 删除群员
                    Boolean result;
                    code = YES;
                    msg = "操作成功！";
                    for (String account : accounts) {
                        result = GroupDao.getInstance().removeMember(groupNo, creator, account, false);
                        if (result == null) {
                            code = NO_PARAMS_ERROR;
                            msg = "请求参数错误，请重新检查";
                            break;
                        } else if (!result) {
                            code = NO_SERVER;
                            msg = "服务器异常，请稍后再试！";
                            break;
                        }
                    }
                }

            }

        }
        response = Response.make(code, msg, null);
        session.server().broadcast(response.toString(), session.client());
        session.loggoer.append("response json: " + GsonUtils.toJson(response) + LINE)
                .append("=================网络请求结束=====================================================================\n\n");
        System.out.println(session.loggoer);
    }

    @Path(path = RequestConfig.Path.getMembers)
    public static void getMembers(CommonSession session) {
        String code;
        String msg;
        Response response = null;

        String groupNosStr = session.params().get(RequestConfig.Key.groupNos);
        if (StringUtils.isEmpty(groupNosStr)) {
            code = NO_PARAMS_MISSING;
            msg = "参数不完整！";
        } else {
            String[] groupNos = GsonUtils.toBean(groupNosStr, String[].class);
            Result result = MemberDao.getInstance().getMembers(groupNos);
            session.loggoer.append(result.logger + LINE);
            if (result.count < 0) {
                code = NO_SERVER;
                msg = "服务器异常，请稍后再试！";
            } else {
                code = YES;
                msg = "获取成员成功！";
                System.out.println("member size: " + result.data.size());
                System.out.println("member size: " + result.count);
                response = Response.make(code, msg, result.data);
            }
        }
        if (response == null) {
            response = Response.make(code, msg, null);
        }
        session.server().broadcast(response.toString(), session.client());
        session.loggoer.append("response json: " + GsonUtils.toJson(response) + LINE)
                .append("=================网络请求结束=====================================================================\n\n");
        System.out.println(session.loggoer);
    }

    @Path(path = RequestConfig.Path.modifyAllowChat)
    public static void modifyAllowChat(CommonSession session) {
        String code;
        String msg;

        String groupNo = session.params().get(RequestConfig.Key.groupNo);
        String account = session.params().get(RequestConfig.Key.account);
        String allowChat = session.params().get(RequestConfig.Key.allowChat);
        String creator = session.params().get(RequestConfig.Key.creator);
        if (StringUtils.isEmpty(groupNo, account, allowChat, creator)) {
            code = NO_PARAMS_MISSING;
            msg = "参数不完整！";
        } else if (!StringConst.TRUE.equalsIgnoreCase(allowChat) && !StringConst.FALSE.equalsIgnoreCase(allowChat)) {
            code = NO_PARAMS_ERROR;
            msg = "参数错误！";
        } else {

            boolean resultOk = MemberDao.getInstance().modifyAllowChat(groupNo, account, allowChat, creator);
            if (resultOk) {
                code = YES;
                msg = StringConst.TRUE.equalsIgnoreCase(allowChat) ? "已允许该成员发言" : "已禁止该成员发言";
            } else {
                code = NO_SERVER;
                msg = "服务器异常，请稍后再试！";
            }
        }
        Response response = Response.make(code, msg, null);
        session.server().broadcast(response.toString(), session.client());
        session.loggoer.append("response json: " + GsonUtils.toJson(response) + LINE)
                .append("=================网络请求结束=====================================================================\n\n");
        System.out.println(session.loggoer);
    }

    @Path(path = RequestConfig.Path.sendMsg)
    public static void sendMsg(CommonSession session) {
        String code;
        String msg;

        Message message = new Message();

        message.setFromAccount(session.params().get(Message.FROM));
        message.setToAccount(session.params().get(Message.TO));
        message.setCreateTime(session.params().get(Message.CREATE_TIME));
        message.setContent(session.params().get(Message.CONTENT));
        message.setContentType(session.params().get(Message.CONTENT_TYPE));
        message.setExtra(session.params().get(Message.EXTRA));


        Result result = MessageDao.getInstance().saveMessage(message);

        if (result.count == 1) {
            code = YES;
        } else {
            code = NO;
        }
        Response response = Response.make(code, "", null);
        session.server().broadcast(response.toString(), session.client());
        session.loggoer.append("response json: " + GsonUtils.toJson(response) + LINE)
                .append("=================网络请求结束=====================================================================\n\n");
        System.out.println(session.loggoer);
    }

}
