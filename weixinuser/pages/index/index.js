// index.js
Page({
  data:{
    msg:'hello world',
    nickName:'',
    url:'',
    code:''
  },
  //获取微信用户的头像和昵称
  getUserInfo(e){
     wx.getUserProfile({
       desc: '获取用户信息',
       success: (res) =>{
         console.log(res.userInfo)
         this.setData({
           nickName: res.userInfo.nickName,
           url: res.userInfo.avatarUrl
         })
       }
       })
  },
  //微笑登陆获取用户的授权码
  wxLogin(){
    wx.login({
      success : (res)=>{
        console.log(res.code)
        this.setData({
          code: res.code
        })
      }
    })
  },
  //发送异步请求
  sendRequst(){
    wx.request({
      url: 'http://localhost:8080/user/shop/status',
      method: 'GET',
      success: (res)=>{
        console.log(res.data)
      }
    })
  }
})
