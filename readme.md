# Introduction to Stars

## Brief
This engine come from an online project called Si Mei Ren.

## Road Map
- adapt spring framework
- simplify the serialization/re-serialization of request/respond
- use aop, e.g. verify pre-condition of request
- change the distribution framework (like dubbo)

## Architecture

## Introduction to Game Server
### Overview
### Player and Module
#### Lifecycle of Module
#### Decouple between Modules
#### Red Point
#### Abstract Data
### Data Loading and Persisting
### Common Service
### An Expression
#### Overview
This expression util supply the basic operation for game, including most used relation operator, logic operator and a
simple select operator. There are some example as follows.

    level > 0                       // the level of player is greater than 0
    1 + 2 == 3                      // the sum of 1 and 2 is equal to 3
    level > 10 && vip > 0           // the level of player is greater than 10 and the vip is greater than 0
    level in (10, 11, 12)           // the level of player is 10, or 11, or 12
    level between (10, 20)          // the level of player is between 10(inclusive) and 20(inclusive)
    [itemBag, quality > 10] > 20    // the count of item in itemBag where quality is greater than 10 is greater than 20
    {isopen, 'friend'}              // call the 'isopen' function, passing the parameter 'friend'
    
#### Priority
| Operator | Description |
| :-----: | ----- |
| ()  | parentheses |
| [] |  |
| {} | |
| ^ | pow |
| *、/、% | mul, div, mod |
| +、- | add, sub |
| ==、!=、>、>=、<、<=、in、between | eq, ne, gt, ge, lt, le, in, btween |
| not、! | not |
| and、&& | and |
| or、&#124;&#124; | or |

#### Basic Rules
- There are two types in the expression, **number** and **string**, which to **long** and **String** in Java.
- The result of relational operation and logical operation will turn into number, non-zero for **true**, 0 for **false**.
- Because **number** is just **long** in Java, **number** behave as what **long** like in Java.
- The **in** operator use for a range check. `x in (u, v, w)` means that `x == u || x == v || x == w`.
- The **between** operator use for a range check. `x between (y, z)` means that `x >= y && x <= z`.
- The **[]** operator like **select** in SQL. 
`[itemBag, quality > 10]` means that `select count(1) from itemBag where quality > 10`. 
Be notice that it is a time consuming operation.
- The **{}** operator use for a function invocation.
`{ismoduleopen, 'friend'}` means that call the function **ismoduleopen** and pass a parameter **'friend'**.

#### Usages

    ExprNode expr = new ExprParser(new ExprLexer("1 + 1")).parse();
    long result = (long) expr.eval();
    
#### Extension Mechanism
    
#### Extent Value

#### Extent Data Set

#### Extent Function

## Introduction to Fight Server
### Overview