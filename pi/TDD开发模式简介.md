# TDD开发模式简介

## 什么是TDD



测试驱动开发(Test-Driven Development)简单来说就是在编写功能代码之前，先进行测试代码的开发，由测试代码确定需要编写什么功能代码



但TDD并不是单纯的测试先行，而是在写功能代码之前先对需求`任务进行设计`，确定或排除模糊不清的任务，并思考这个任务将`如何实现和测试`，并将以上建模落实到测试用例代码中。



#### 规则



- 仅在自动测试失败时才编写新代码。
- 消除重复设计（去除不必要的依赖关系），优化设计结构（逐渐使代码一般化）。



> 核心：先设计、再开发，将设计建模落实在测试代码中。



#### 原则



- 测试驱动：首先编写测试代码，明确这个方法或类如何使用，如何测试，并对其进行设计和编码。
- 先写断言：编写测试代码时，应先确定断言语句，再完善辅助语句。
- 即时重构：对结构不合理或可读性较差的功能代码，在测试通过后立刻重构它。
- 小步快跑：将需求任务通过测试代码拆解成小目标，每次只新增足够使测试用例通过的功能代码。确保问题只会出在新增的代码中。



> 刚写完`可用`代码的时候就是重构的最好时机



## 怎么做TDD



![TDD-cycle.png](https://cdn.nlark.com/yuque/0/2020/png/679850/1594650442036-42f546d5-382f-40b7-9632-4eedfcbc5835.png)

- Step1: 编写一个最简单的测试case1 （想一想我要做什么，如何测试它）
- Step2: 编写足够的代码使测试失败（明确失败总比模模糊糊的感觉要好）
- Step3: 编写刚刚好使测试case1通过的代码（保证之前编写的测试也需要通过），运行并观察所测试，有问题就立即解决它，问题一定出现在最新引入的代码中
- Step4: 如果有任何重复的逻辑或无法解释的代码，重构可以消除重复并提高表达能力（减少耦合，增加内聚力）
- Step5: 再次运行测试验证重构是否引入新的错误。如果没有通过，很可能是在重构时犯了一些错误，需要立即修复并重新运行，直到所有测试通过
- Step6: 重复以上步骤，完成所有case的覆盖



## 与传统开发的对比



TDD与传统开发流程不同，以伪代码的形式展示如下：



```
// TDD：测试驱动开发
//需求场景包括正常路径+边界情况+异常情况
for(feature in features) {

  testCases.add(caseCover(feature))

  while (!run(testCases)) {

    write(code)
    //不断设计和结构化代码
    refactor(testCases, code)   
   }
}
```



```
// 传统开发
write(code)

for(feature in features){

  testCases.add(caseCover(feature))

  while (!run(testCases)){
     //不断修改bug，可能不存在重构
    fix(code)
    fix(testCases)
  }
}
```



#### 写功能代码前的准备工作



- TDD模式：写代码前思考如何实现功能并测试它，可以立刻识别出模糊不清的任务点，将建模落实在测试用例中。
- 传统模式：根据开发者自己对于任务的理解，脑内组织或纸上简单画两笔，缺乏准确建模。



#### write(code)：写功能代码时的思维差别



- TDD模式：写功能代码时只保证当前测试用例可以通过。相当于将任务细分为一个小目标，并准确的实现它。
- 传统模式：根据个人对任务理解开始编码，最终实现的代码更加偏向于开发者自身的逻辑思考路径。



#### caseCover(feature)：写测试代码的质量差距



- TDD模式：不考虑功能代码如何实现，只考虑需求场景的测试，写出的测试代码更加简洁、更贴近需求场景。
- 传统模式：下意识会根据已有功能代码做了什么事情而去编写测试代码，可能会脱离真实需求场景。或者因为需求代码本身不易于测试，导致测试代码复杂化。测试代码一旦复杂，不确定性就会增加，用不确定的代码去测试功能代码，结果可能是灾难性的。



> 测试代码质量与熟练度息息相关，多思考多练习才能提升测试代码质量。



#### refactor与fix的区别



- refactor: 在测试代码的安全网中，不断进行微小的重构，并立刻得到反馈。出错的代码一定在刚刚改动的地方，可以快速找到问题所在。在这个过程中一边实现功能需求，一边优化代码结构。
- fix：实际上是一个debug过程，先将bug洒出去，再一个个找回来，错误范围不明确，比较耗时。



## 一个Demo



> 用户故事：作为用户，我想要通过手机号和验证码注册/登录系统



- given:我在注册/登录页
- when:填入手机号和验证码
- then:可以登录系统
- note:

1. 1. 用户输入手机号只能输入纯数字
   2. 用户收入手机号只能输入11位，超过不能输入
   3. 用户输入验证码只能输入纯数字
   4. 用户输入验证码只能输入6位



#### 分析用户故事



- 获取用户在手机号`input`中输入的`value`，对`value`进行一些限制处理，并实时反映到界面中。



#### 开始开发



第一步：新增获取用户输入的手机号码方法



```
//login-box.component.spec.ts

  it('step 1 获取输入手机号',()=>{
    let value = '12'
    expect(component.getPhoneNumberInput(value)).toBe(value)

  })
  
  
//login-box.component.ts

  getPhoneNumberInput(value: string): string {
    return value
  }
```



第二步：获取用户输入的纯数字部分



```
//login-box.component.spec.ts

  it('step 1 获取输入手机号',()=>{
    let number = '12'
    expect(component.getPhoneNumberInput(number)).toBe(number)

  })

  it('step 2 获取输入手机号，确保只能输入数字',()=>{
    let number = '12_(adfasfa3'
    expect(component.getPhoneNumberInput(number)).toBe('123')
  })
  
  
//login-box.component.ts

  getPhoneNumberInput(value: string): string {
    return value.replace(/\D/g, '')
  }
```



第三步：获取用户输入的纯数字部分



```
//login-box.component.spec.ts

  it('step 1 获取输入手机号',()=>{
    let number = '12'
    expect(component.getPhoneNumberInput(number)).toBe(number)

  })

  it('step 2 获取输入手机号，确保只能输入数字',()=>{
    let number = '12_(adfasfa3'
    expect(component.getPhoneNumberInput(number)).toBe('123')
  })
  
  it('step 3 获取输入手机号，确保只能输入数字，只取前11位数字',()=>{
    let number = '12_(adfasfa3456789da01234'
    expect(component.getPhoneNumberInput(number)).toBe('12345678901')
  })
  
//login-box.component.ts

  getPhoneNumberInput(value: string): string {
    return value.replace(/\D/g, '').substring(0, 11)
  }
```



第四步：考虑一些边界或异常情况



```
//login-box.component.spec.ts

  it('step 1 获取输入手机号',()=>{
    let number = '12'
    expect(component.getPhoneNumberInput(number)).toBe(number)

  })

  it('step 2 获取输入手机号，确保只能输入数字',()=>{
    let number = '12_(adfasfa3'
    expect(component.getPhoneNumberInput(number)).toBe('123')
  })
  
  it('step 3 获取输入手机号，确保只能输入数字，只取前11位数字',()=>{
    let number = '12_(adfasfa3456789da01234'
    expect(component.getPhoneNumberInput(number)).toBe('12345678901')
  })
  
  it('step 4 获取输入手机号，考虑一些边界情况',()=>{
    expect(component.getPhoneNumberInput('')).toBe('')
    expect(component.getPhoneNumberInput(null)).toBe('')
    expect(component.getPhoneNumberInput(undefined)).toBe('')
  })
  
//login-box.component.ts

  getPhoneNumberInput(value: string): string {
    if(!value){
      return ''
    }
    return value.replace(/\D/g, '').substring(0, 11)
  }
```



到此note-a和note-b都已经被覆盖。此时可以开始着手实现note-c和note-d，经过分析，验证码`input`与手机号`input`的约束条件存在交集，只有位数约束条件不同。可以考虑重构已有的`getPhoneNumberInput(value: string)`方法，来实现同时覆盖全部notes。



- 为了保持代码的可读性，我们将方法名称修改为：`getNumberInput(value: string, length: number)`
- 测试代码可以在原有基础上进行修改，并合并成一个`it`



第五步：重构已有实现，添加对于`value`长度的动态限制



```
//login-box.component.spec.ts

  it('step 5 获取数字输入的方法，超出位数截取',()=>{
    let number = '12'
    let length = 6
    expect(component.getNumberInput(number,length)).toBe(number)
    number = '12_(adfasfa3'
    length = 6
    expect(component.getNumberInput(number,length)).toBe('123')
    number = '12_(adfasfa3456789da01234'
    length = 6
    expect(component.getNumberInput(number,length)).toBe('123456')
    number = '12_(adfasfa3456789da01234'
    length = 11
    expect(component.getNumberInput(number,length)).toBe('12345678901')

    //测试一些边界条件
    expect(component.getNumberInput('',length)).toBe('')
    expect(component.getNumberInput(null,length)).toBe('')
    expect(component.getNumberInput(undefined,length)).toBe('')
  })
  
  //login-box.component.ts

  getNumberInput(value: string,length: number): string {
    if(!value){
      return ''
    }
    return value.replace(/\D/g, '').substring(0, length)
  }
```



第六步：再考虑一下边界或异常情况



```
//login-box.component.spec.ts

  it('step 5 获取数字输入的方法，超出位数截取',()=>{
    let number = '12'
    let length = 6
    expect(component.getNumberInput(number,length)).toBe(number)
    number = '12_(adfasfa3'
    length = 6
    expect(component.getNumberInput(number,length)).toBe('123')
    number = '12_(adfasfa3456789da01234'
    length = 6
    expect(component.getNumberInput(number,length)).toBe('123456')
    number = '12_(adfasfa3456789da01234'
    length = 11
    expect(component.getNumberInput(number,length)).toBe('12345678901')

    //测试一些边界条件
    expect(component.getNumberInput('',length)).toBe('')
    expect(component.getNumberInput(null,length)).toBe('')
    expect(component.getNumberInput(undefined,length)).toBe('')
  })
  
  it('step 6 获取数字输入的方法，按照位数截取 边界条件',()=>{
    //测试一些边界条件
    expect(component.getNumberInput('123',-1)).toBe('')
    expect(component.getNumberInput('1231',0)).toBe('')
    expect(component.getNumberInput('123',null)).toBe('')
    expect(component.getNumberInput('123',undefined)).toBe('')
  })
  
  //login-box.component.ts

  getNumberInput(value: string,length: number): string {
    if(!number || !length || length<=0 ){
      return ''
    }
    return value.replace(/\D/g, '').substring(0, length)
  }
```



## TDD的目标



帮助我们用最短路径写出“可用”代码，并一定程度上保证了代码的“可测试性”、“可读性”、“可维护性”。同时为我们提供了可靠的重构条件。



## TDD的优点



#### 提前澄清需求



先写测试可以帮助我们去思考需求，并提前澄清需求细节，而不是代码写到一半才发现不明确的需求。



#### 降低开发者负担



TDD是一种代码开发的方式，通过明确的流程，让开发者聚焦于当前的一个小目标，将复杂的思考过程逐步简化各个击破，需求越是复杂收益越大。



#### 快速反馈



单元测试是功能代码的第一个用户，出现问题可以快速明晰的反馈。相对于先进行功能开发，再手动测试的方式，要节省许多反馈时间。



#### 保护网



TDD确保你的所有功能代码都在测试代码的覆盖之下。此时测试代码就是功能代码的保护网，让我们可以轻松地迎接需求变化或改善代码的设计。需求迭代或重构越多保护网收益越大。



#### 节省更多时间



开发和维护阶段都是软件较为耗时的阶段。提前澄清需求和快速反馈可以在开发阶段节省时间。保护网则可以在维护阶段节省时间。提测时整体质量提升，还可以节省测试阶段时间。



#### 自带说明文档



测试代码就是功能代码的说明文档，它说明了方法和类如何使用并且提供了实例。



## 使用TDD需要注意什么



#### TDD不是万能模式，不是银弹



- TDD不能保证你在开发初期就将测试用例编写完整，开发完成后可能还需要根据需求场景补充边界或异常测试条件
- 使用TDD并不能让开发者写出完美的测试代码和功能代码，这一切取决于开发者自身的能力。TDD只是一种模式，帮助开发者建立思维模型。同时也是一面镜子，帮助开发者发现自身的不足。



#### TDD需要开发者转换思维方式



- 以用户的视角去思考如何使用功能代码，这是开发者很少会做的事情，但是TDD以测试用例驱动开发者这样做。
- 先写测试代码再写功能代码，这与开发者传统的思维方法不一致，需要花一些时间适应。



#### 利用TDD模式，让测试代码为你服务，而不是成为你的累赘



- 使用测试代码，哪怕是跑失败的测试代码，来快速验证你的想法以及功能代码实现。



#### TDD并不能保证你一定会写出优雅的代码



- 让代码审查员review你的代码，保证它的“可读性”
- 不断的写和优化测试代码，保证代码的“可测性”
- 提升重构和设计代码的能力，保证代码的“可维护性”
- 多读好的代码，提升对代码的品味
- TDD只是提供了一种模式，帮助开发者关注以上4步，发现自己的不足。