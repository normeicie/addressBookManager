package com.von.txl;

import com.von.txl.bean.ColumnInfo;
import com.von.txl.bean.LabelName;
import com.von.txl.bean.NameCard;
import com.von.txl.util.StringLengthUtil;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {
    private static final int PAGE_SIZE = 5;
    private static final int CURRENT_PAGE = 1;
    private static final int MAX_RECORD_SIZE = 5000;
    private static final int FILE_BACKUP_COUNT = 5;
    private static final String DEFAULT_FILENAME = "txl.sdb";
    private static final String DATA_DEFAULT_DELIMITER = "\u0001";
    private static final List<NameCard> NAME_CARD_LIST = Collections.synchronizedList(new ArrayList<>(MAX_RECORD_SIZE));

    private static final String TIPS_1 = "无效的功能编号！请重新输入：";
    private static final String TIPS_2 = "输入序号不正确！请重新输入：";

    public static void main(String[] args) throws IllegalAccessException, IOException {
        String bookInfoPath = System.getProperty("user.home");
        bookInfoPath = bookInfoPath.replaceAll("\\\\", "/");
        bookInfoPath = (bookInfoPath.endsWith("/") ? bookInfoPath : bookInfoPath + "/") + DEFAULT_FILENAME;
        System.out.println("欢迎使用个人通讯录管理系统");
        System.out.println("开始初始化通讯薄：" + bookInfoPath);
        try {
            getNameCardList(bookInfoPath);
        } catch (IOException e) {
            System.out.println("初始化通讯薄异常。");
        }
        System.out.println("初始化通讯薄完成。共加载[" + NAME_CARD_LIST.size() + "]条记录");
        printMainMenu();
        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNext()) {
                String data = scanner.next();
                System.out.println("你的输入是： < " + data + " >");
                if (data.equals("0")) {
                    System.out.print("确定退出？[Yes/No]:");
                    while (scanner.hasNext()) {
                        data = scanner.next();
                        if (data.equalsIgnoreCase("y") || data.equalsIgnoreCase("yes")) {
                            System.out.println("你已选择退出！");
                            System.exit(0);
                        }
                        printMainMenu();
                        break;
                    }
                } else {
                    switch (data) {
                        case "1":
                            System.out.println("你进入了：--> 通讯录列表 <-- 功能");
                            listExit:
                            while (true) {
                                if (NAME_CARD_LIST.isEmpty()) {
                                    System.out.println("暂无数据！");
                                    break;
                                } else {
                                    int total = NAME_CARD_LIST.size();
                                    int currentPage = CURRENT_PAGE;
                                    int pageSize = PAGE_SIZE;
                                    int totalPage = total % pageSize == 0 ? total / pageSize : (total / pageSize) + 1;
                                    printInfo(getPageData(NAME_CARD_LIST, currentPage, pageSize), false);
                                    printListMenu(total, totalPage, currentPage, pageSize);
                                    while (scanner.hasNext()) {
                                        data = scanner.next();
                                        System.out.println("你的输入是： < " + data + " >");
                                        try {
                                            if ("0".equals(data)) {
                                                break listExit;
                                            } else if ("1".equals(data)) {
                                                currentPage = CURRENT_PAGE;
                                                printInfo(getPageData(NAME_CARD_LIST, CURRENT_PAGE, pageSize), false);
                                                printListMenu(total, totalPage, currentPage, pageSize);
                                            } else if ("2".equals(data)) {
                                                currentPage = currentPage < totalPage ? currentPage + 1 : totalPage;
                                                printInfo(getPageData(NAME_CARD_LIST, currentPage, pageSize), false);
                                                printListMenu(total, totalPage, currentPage, pageSize);
                                            } else if ("3".equals(data)) {
                                                currentPage = currentPage > 1 ? currentPage - 1 : CURRENT_PAGE;
                                                printInfo(getPageData(NAME_CARD_LIST, currentPage, pageSize), false);
                                                printListMenu(total, totalPage, currentPage, pageSize);
                                            } else if ("4".equals(data)) {
                                                currentPage = totalPage;
                                                printInfo(getPageData(NAME_CARD_LIST, currentPage, pageSize), false);
                                                printListMenu(total, totalPage, currentPage, pageSize);
                                            } else {
                                                System.out.print(TIPS_1);
                                            }
                                        } catch (Exception e) {
                                            System.out.print(TIPS_1);
                                        }
                                    }
                                }
                            }
                            printMainMenu();
                            break;
                        case "2":
                            System.out.println("你进入了：--> 新增联系人 <-- 功能");
                            addExit:
                            while (true) {
                                System.out.println("请根据提示输入对应的输入项[以下为必填项]：");
                                NameCard nameCard = new NameCard();
                                Field[] fields = NameCard.class.getDeclaredFields();
                                Map<Integer, String> skipMap = new HashMap<>();
                                for (Field field : fields) {
                                    field.setAccessible(true);
                                    LabelName labelName = field.getAnnotation(LabelName.class);
                                    next:
                                    while (!labelName.isNull()) {
                                        while (true) {
                                            System.out.print("请输入 [" + labelName.value() + "]: ");
                                            while (scanner.hasNext()) {
                                                data = scanner.next();
                                                final String checkData = data;
                                                List<NameCard> nameCards = NAME_CARD_LIST.stream().filter(n -> checkData.equalsIgnoreCase(n.getUserName())).collect(Collectors.toList());
                                                if (null == nameCards || nameCards.isEmpty()) {
                                                    String checkRule = labelName.checkRule();
                                                    if (null == checkRule || checkRule.isEmpty()) {
                                                        field.set(nameCard, data);
                                                        break next;
                                                    } else {
                                                        Matcher matcher = Pattern.compile(checkRule).matcher(data);
                                                        if (matcher.matches()) {
                                                            field.set(nameCard, data);
                                                            break next;
                                                        } else {
                                                            System.out.println("输入值不符合校验规则！" + labelName.checkRuleDesc());
                                                            break;
                                                        }
                                                    }
                                                } else {
                                                    System.out.println("已存在同名联系人！" + nameCards.get(0).toString());
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    if (labelName.isNull()) {
                                        skipMap.put(labelName.orderIndex(), labelName.value());
                                    }
                                }
                                printAdd(skipMap);
                                addSave:
                                while (true) {
                                    addNext:
                                    while (scanner.hasNext()) {
                                        data = scanner.next();
                                        try {
                                            int chooseNumber = Integer.parseInt(data);
                                            if (chooseNumber == 0) {
                                                break addSave;
                                            } else {
                                                if (skipMap.containsKey(chooseNumber)) {
                                                    while (true) {
                                                        System.out.print("请输入已选编号 [" + chooseNumber + "] " + skipMap.get(chooseNumber) + " 对应的值：");
                                                        checkRuleExit:
                                                        while (scanner.hasNext()) {
                                                            data = scanner.next();
                                                            fields = NameCard.class.getDeclaredFields();
                                                            for (Field field : fields) {
                                                                field.setAccessible(true);
                                                                LabelName labelName = field.getAnnotation(LabelName.class);
                                                                if (chooseNumber == labelName.orderIndex()) {
                                                                    String checkRule = labelName.checkRule();
                                                                    if (null == checkRule || checkRule.isEmpty()) {
                                                                        field.set(nameCard, data);
                                                                        printAdd(skipMap);
                                                                        break addNext;
                                                                    } else {
                                                                        Matcher matcher = Pattern.compile(checkRule).matcher(data);
                                                                        if (matcher.matches()) {
                                                                            field.set(nameCard, data);
                                                                            printAdd(skipMap);
                                                                            break addNext;
                                                                        } else {
                                                                            System.out.println("输入值不符合校验规则！" + labelName.checkRuleDesc());
                                                                            break checkRuleExit;
                                                                        }
                                                                    }

                                                                }
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    System.out.print(TIPS_2);
                                                }
                                            }
                                        } catch (Exception e) {
                                            System.out.print(TIPS_2);
                                        }
                                    }
                                }
                                System.out.println("新增信息为: " + nameCard);
                                NAME_CARD_LIST.add(nameCard);
                                saveToFile(bookInfoPath);
                                System.out.print("是否继续添加新用户？[Yes/No]：");
                                while (scanner.hasNext()) {
                                    data = scanner.next();
                                    if (data.equalsIgnoreCase("y") || data.equalsIgnoreCase("yes")) {
                                        break;
                                    } else {
                                        break addExit;
                                    }
                                }
                            }
                            printMainMenu();
                            break;
                        case "3":
                            System.out.println("你进入了：--> 编辑联系人 <-- 功能");
                            editExit:
                            while (true) {
                                if (NAME_CARD_LIST.isEmpty()) {
                                    System.out.println("暂无数据！");
                                    break;
                                } else {
                                    int currentPage = CURRENT_PAGE;
                                    int pageSize = PAGE_SIZE;
                                    while (true) {
                                        int total = NAME_CARD_LIST.size();
                                        int totalPage = total % pageSize == 0 ? total / pageSize : (total / pageSize) + 1;
                                        printInfo(getPageData(NAME_CARD_LIST, currentPage, pageSize), true);
                                        printEditListMenu(total, totalPage, currentPage, pageSize);
                                        while (scanner.hasNext()) {
                                            data = scanner.next();
                                            System.out.println("你的输入是： < " + data + " >");
                                            try {
                                                int num = Integer.parseInt(data);
                                                if (num == 0) {
                                                    break editExit;
                                                } else if (num == 1) {
                                                    currentPage = CURRENT_PAGE;
                                                    printInfo(getPageData(NAME_CARD_LIST, CURRENT_PAGE, pageSize), true);
                                                    printEditListMenu(total, totalPage, currentPage, pageSize);
                                                } else if (num == 2) {
                                                    currentPage = currentPage < totalPage ? currentPage + 1 : totalPage;
                                                    printInfo(getPageData(NAME_CARD_LIST, currentPage, pageSize), true);
                                                    printEditListMenu(total, totalPage, currentPage, pageSize);
                                                } else if (num == 3) {
                                                    currentPage = currentPage > 1 ? currentPage - 1 : CURRENT_PAGE;
                                                    printInfo(getPageData(NAME_CARD_LIST, currentPage, pageSize), true);
                                                    printEditListMenu(total, totalPage, currentPage, pageSize);
                                                } else if (num == 4) {
                                                    currentPage = totalPage;
                                                    printInfo(getPageData(NAME_CARD_LIST, currentPage, pageSize), true);
                                                    printEditListMenu(total, totalPage, currentPage, pageSize);
                                                } else if (num == 5) {
                                                    NameCard nameCard = null;
                                                    editCompleted:
                                                    while (true) {
                                                        int currentRecordSize = total > (currentPage * pageSize) ? pageSize : total - ((currentPage - 1) * pageSize);
                                                        System.out.print("请输入当前编辑列表记录前的数字[1~" + currentRecordSize + "]进行对应记录的信息编辑，[0] 退出编辑模式：");
                                                        while (scanner.hasNext()) {
                                                            data = scanner.next();
                                                            try {
                                                                num = Integer.parseInt(data);
                                                                if (num == 0) {
                                                                    break editCompleted;
                                                                } else {
                                                                    if (num <= currentRecordSize) {
                                                                        Map<Integer, String> editMap = new HashMap<>();
                                                                        nameCard = NAME_CARD_LIST.get(((currentPage - 1) * pageSize) + num - 1);
                                                                        Field[] fields = NameCard.class.getDeclaredFields();
                                                                        for (Field field : fields) {
                                                                            field.setAccessible(true);
                                                                            LabelName labelName = field.getAnnotation(LabelName.class);
                                                                            Object value = field.get(nameCard);
                                                                            editMap.put(labelName.orderIndex(), labelName.value() + ": " + (null == value ? "" : value.toString()));
                                                                        }
                                                                        while (true) {
                                                                            printEdit(editMap);
                                                                            saveEdit:
                                                                            while (scanner.hasNext()) {
                                                                                data = scanner.next();
                                                                                try {
                                                                                    num = Integer.parseInt(data);
                                                                                    if (num == 0) {
                                                                                        break editCompleted;
                                                                                    } else {
                                                                                        if (editMap.containsKey(num)) {
                                                                                            while (true) {
                                                                                                System.out.print("请输入已选编号 [" + num + "] " + editMap.get(num) + " 对应的新值：");
                                                                                                checkRuleExit:
                                                                                                while (scanner.hasNext()) {
                                                                                                    data = scanner.next();
                                                                                                    for (Field field : fields) {
                                                                                                        field.setAccessible(true);
                                                                                                        LabelName labelName = field.getAnnotation(LabelName.class);
                                                                                                        if (num == labelName.orderIndex()) {
                                                                                                            if (num == 1) {
                                                                                                                final String sourceName = nameCard.getUserName();
                                                                                                                final String checkName = data;
                                                                                                                List<NameCard> nameCards = NAME_CARD_LIST.stream()
                                                                                                                        .filter(n -> !sourceName.equalsIgnoreCase(n.getUserName()))
                                                                                                                        .filter(n -> checkName.equalsIgnoreCase(n.getUserName())).collect(Collectors.toList());
                                                                                                                if (null == nameCards || nameCards.isEmpty()) {
                                                                                                                    String checkRule = labelName.checkRule();
                                                                                                                    if (null == checkRule || checkRule.isEmpty()) {
                                                                                                                        field.set(nameCard, data);
                                                                                                                        editMap.put(labelName.orderIndex(), labelName.value() + ": " + (null == data ? "" : data));
                                                                                                                        break saveEdit;
                                                                                                                    } else {
                                                                                                                        Matcher matcher = Pattern.compile(checkRule).matcher(data);
                                                                                                                        if (matcher.matches()) {
                                                                                                                            field.set(nameCard, data);
                                                                                                                            editMap.put(labelName.orderIndex(), labelName.value() + ": " + (null == data ? "" : data));
                                                                                                                            break saveEdit;
                                                                                                                        } else {
                                                                                                                            System.out.println("输入值不符合校验规则！" + labelName.checkRuleDesc());
                                                                                                                            break checkRuleExit;
                                                                                                                        }
                                                                                                                    }
                                                                                                                } else {
                                                                                                                    System.out.println("已存在同名联系人！" + nameCards.get(0).toString());
                                                                                                                    break checkRuleExit;
                                                                                                                }
                                                                                                            } else {
                                                                                                                String checkRule = labelName.checkRule();
                                                                                                                if (null == checkRule || checkRule.isEmpty()) {
                                                                                                                    field.set(nameCard, data);
                                                                                                                    editMap.put(labelName.orderIndex(), labelName.value() + ": " + (null == data ? "" : data));
                                                                                                                    break saveEdit;
                                                                                                                } else {
                                                                                                                    Matcher matcher = Pattern.compile(checkRule).matcher(data);
                                                                                                                    if (matcher.matches()) {
                                                                                                                        field.set(nameCard, data);
                                                                                                                        editMap.put(labelName.orderIndex(), labelName.value() + ": " + (null == data ? "" : data));
                                                                                                                        break saveEdit;
                                                                                                                    } else {
                                                                                                                        System.out.println("输入值不符合校验规则！" + labelName.checkRuleDesc());
                                                                                                                        break checkRuleExit;
                                                                                                                    }
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        } else {
                                                                                            System.out.print(TIPS_2);
                                                                                        }
                                                                                    }
                                                                                } catch (Exception e) {
                                                                                    System.out.print(TIPS_2);
                                                                                }
                                                                            }
                                                                        }
                                                                    } else {
                                                                        System.out.print("序号只能为：[1~" + currentRecordSize + "]之间的数字。" + TIPS_2);
                                                                    }
                                                                }
                                                            } catch (Exception e) {
                                                                System.out.print(TIPS_2);
                                                            }
                                                        }
                                                    }
                                                    if (null != nameCard) {
                                                        System.out.println("修改后的信息为: " + nameCard);
                                                        saveToFile(bookInfoPath);
                                                    } else {
                                                        System.out.println("已退出编辑模式");
                                                    }
                                                    break;
                                                } else {
                                                    System.out.print(TIPS_1);
                                                }
                                            } catch (Exception e) {
                                                System.out.print(TIPS_1);
                                            }
                                        }
                                    }
                                }
                            }
                            printMainMenu();
                            break;
                        case "4":
                            System.out.println("你进入了：--> 删除联系人 <-- 功能");
                            deleteExit:
                            while (true) {
                                if (NAME_CARD_LIST.isEmpty()) {
                                    System.out.println("暂无数据！");
                                } else {
                                    int currentPage = CURRENT_PAGE;
                                    int pageSize = PAGE_SIZE;
                                    while (true) {
                                        int total = NAME_CARD_LIST.size();
                                        int totalPage = total % pageSize == 0 ? total / pageSize : (total / pageSize) + 1;
                                        printInfo(getPageData(NAME_CARD_LIST, currentPage > totalPage ? totalPage : currentPage, pageSize), true);
                                        printDeleteListMenu(total, totalPage, currentPage > totalPage ? totalPage : currentPage, pageSize);
                                        while (scanner.hasNext()) {
                                            data = scanner.next();
                                            System.out.println("你的输入是： < " + data + " >");
                                            try {
                                                int num = Integer.parseInt(data);
                                                if (num == 0) {
                                                    break deleteExit;
                                                } else if (num == 1) {
                                                    currentPage = CURRENT_PAGE;
                                                    printInfo(getPageData(NAME_CARD_LIST, CURRENT_PAGE, pageSize), true);
                                                    printDeleteListMenu(total, totalPage, currentPage, pageSize);
                                                } else if (num == 2) {
                                                    currentPage = currentPage < totalPage ? currentPage + 1 : totalPage;
                                                    printInfo(getPageData(NAME_CARD_LIST, currentPage, pageSize), true);
                                                    printDeleteListMenu(total, totalPage, currentPage, pageSize);
                                                } else if (num == 3) {
                                                    currentPage = currentPage > 1 ? currentPage - 1 : CURRENT_PAGE;
                                                    printInfo(getPageData(NAME_CARD_LIST, currentPage, pageSize), true);
                                                    printDeleteListMenu(total, totalPage, currentPage, pageSize);
                                                } else if (num == 4) {
                                                    currentPage = totalPage;
                                                    printInfo(getPageData(NAME_CARD_LIST, currentPage, pageSize), true);
                                                    printDeleteListMenu(total, totalPage, currentPage, pageSize);
                                                } else if (num == 5) {
                                                    deleteCompleted:
                                                    while (true) {
                                                        int currentRecordSize = total > (currentPage * pageSize) ? pageSize : total - ((currentPage - 1) * pageSize);
                                                        System.out.print("请输入当前列表记录前的数字[1~" + currentRecordSize + "]进行对应记录的信息删除，[0] 退出删除模式：");
                                                        while (scanner.hasNext()) {
                                                            data = scanner.next();
                                                            try {
                                                                num = Integer.parseInt(data);
                                                                if (num == 0) {
                                                                    break deleteCompleted;
                                                                } else {
                                                                    if (num <= currentRecordSize) {
                                                                        System.out.print("确定删除？[Yes/No]:");
                                                                        while (scanner.hasNext()) {
                                                                            String isOk = scanner.next();
                                                                            if (isOk.equalsIgnoreCase("y") || isOk.equalsIgnoreCase("yes")) {
                                                                                NameCard nameCard = NAME_CARD_LIST.get(((currentPage - 1) * pageSize) + num - 1);
                                                                                if (NAME_CARD_LIST.remove(nameCard)) {
                                                                                    System.out.println("删除成功" + nameCard);
                                                                                    saveToFile(bookInfoPath);
                                                                                }
                                                                            }
                                                                            break deleteCompleted;
                                                                        }
                                                                    } else {
                                                                        System.out.print("序号只能为：[1~" + currentRecordSize + "]之间的数字。" + TIPS_2);
                                                                    }
                                                                }
                                                            } catch (Exception e) {
                                                                System.out.print(TIPS_1);
                                                            }
                                                        }
                                                    }
                                                    break;
                                                } else {
                                                    System.out.print(TIPS_1);
                                                }
                                            } catch (Exception e) {
                                                System.out.print(TIPS_1);
                                            }
                                        }
                                    }
                                }
                            }
                            printMainMenu();
                            break;
                        case "5":
                            System.out.println("你进入了：--> 查询联系人 <-- 功能");
                            queryExit:
                            while (true) {
                                printQueryMenu();
                                queryList:
                                while (true) {
                                    while (scanner.hasNext()) {
                                        data = scanner.next();
                                        try {
                                            int num = Integer.parseInt(data);
                                            if ("0".equals(data)) {
                                                break queryExit;
                                            } else if ("1".equals(data)) {
                                                System.out.print("请输入需要查询的用户名：");
                                                while (scanner.hasNext()) {
                                                    data = scanner.next();
                                                    final String queryStr = data;
                                                    List<NameCard> queryReaultList = NAME_CARD_LIST.stream()
                                                            .filter((item) -> item.getUserName().contains(queryStr))
                                                            .collect(Collectors.toList());
                                                    if (queryReaultList.isEmpty()) {
                                                        System.out.println("未查询到用户名包含 " + queryStr + " 的数据！");
                                                        break queryList;
                                                    } else {
                                                        int total = queryReaultList.size();
                                                        int currentPage = CURRENT_PAGE;
                                                        int pageSize = PAGE_SIZE;
                                                        int totalPage = total % pageSize == 0 ? total / pageSize : (total / pageSize) + 1;
                                                        printInfo(getPageData(queryReaultList, currentPage, pageSize), false);
                                                        printQueryListMenu(total, totalPage, currentPage, pageSize);
                                                        while (scanner.hasNext()) {
                                                            data = scanner.next();
                                                            System.out.println("你的输入是： < " + data + " >");
                                                            try {
                                                                if ("0".equals(data)) {
                                                                    break queryExit;
                                                                } else if ("1".equals(data)) {
                                                                    currentPage = CURRENT_PAGE;
                                                                    printInfo(getPageData(queryReaultList, CURRENT_PAGE, pageSize), false);
                                                                    printQueryListMenu(total, totalPage, currentPage, pageSize);
                                                                } else if ("2".equals(data)) {
                                                                    currentPage = currentPage < totalPage ? currentPage + 1 : totalPage;
                                                                    printInfo(getPageData(queryReaultList, currentPage, pageSize), false);
                                                                    printQueryListMenu(total, totalPage, currentPage, pageSize);
                                                                } else if ("3".equals(data)) {
                                                                    currentPage = currentPage > 1 ? currentPage - 1 : CURRENT_PAGE;
                                                                    printInfo(getPageData(queryReaultList, currentPage, pageSize), false);
                                                                    printQueryListMenu(total, totalPage, currentPage, pageSize);
                                                                } else if ("4".equals(data)) {
                                                                    currentPage = totalPage;
                                                                    printInfo(getPageData(queryReaultList, currentPage, pageSize), false);
                                                                    printQueryListMenu(total, totalPage, currentPage, pageSize);
                                                                } else if ("5".equals(data)) {
                                                                    break queryList;
                                                                } else {
                                                                    System.out.print(TIPS_1);
                                                                }
                                                            } catch (Exception e) {
                                                                System.out.print(TIPS_1);
                                                            }
                                                        }
                                                    }
                                                }
                                            } else if ("2".equals(data)) {
                                                System.out.print("请输入需要查询的手机号码：");
                                                while (scanner.hasNext()) {
                                                    data = scanner.next();
                                                    final String queryStr = data;
                                                    List<NameCard> queryReaultList = NAME_CARD_LIST.stream()
                                                            .filter((item) -> item.getMobile().contains(queryStr))
                                                            .collect(Collectors.toList());
                                                    if (queryReaultList.isEmpty()) {
                                                        System.out.println("未查询到手机号包含 " + queryStr + " 的数据！");
                                                        break queryList;
                                                    } else {
                                                        int total = queryReaultList.size();
                                                        int currentPage = CURRENT_PAGE;
                                                        int pageSize = PAGE_SIZE;
                                                        int totalPage = total % pageSize == 0 ? total / pageSize : (total / pageSize) + 1;
                                                        printInfo(getPageData(queryReaultList, currentPage, pageSize), false);
                                                        printQueryListMenu(total, totalPage, currentPage, pageSize);
                                                        while (scanner.hasNext()) {
                                                            data = scanner.next();
                                                            System.out.println("你的输入是： < " + data + " >");
                                                            try {
                                                                if ("0".equals(data)) {
                                                                    break queryExit;
                                                                } else if ("1".equals(data)) {
                                                                    currentPage = CURRENT_PAGE;
                                                                    printInfo(getPageData(queryReaultList, CURRENT_PAGE, pageSize), false);
                                                                    printQueryListMenu(total, totalPage, currentPage, pageSize);
                                                                } else if ("2".equals(data)) {
                                                                    currentPage = currentPage < totalPage ? currentPage + 1 : totalPage;
                                                                    printInfo(getPageData(queryReaultList, currentPage, pageSize), false);
                                                                    printQueryListMenu(total, totalPage, currentPage, pageSize);
                                                                } else if ("3".equals(data)) {
                                                                    currentPage = currentPage > 1 ? currentPage - 1 : CURRENT_PAGE;
                                                                    printInfo(getPageData(queryReaultList, currentPage, pageSize), false);
                                                                    printQueryListMenu(total, totalPage, currentPage, pageSize);
                                                                } else if ("4".equals(data)) {
                                                                    currentPage = totalPage;
                                                                    printInfo(getPageData(queryReaultList, currentPage, pageSize), false);
                                                                    printQueryListMenu(total, totalPage, currentPage, pageSize);
                                                                } else if ("5".equals(data)) {
                                                                    break queryList;
                                                                } else {
                                                                    System.out.print(TIPS_1);
                                                                }
                                                            } catch (Exception e) {
                                                                System.out.print(TIPS_1);
                                                            }
                                                        }
                                                    }
                                                }
                                            } else {
                                                System.out.print(TIPS_1);
                                            }
                                        } catch (Exception e) {
                                            System.out.print(TIPS_1);
                                        }
                                    }
                                }
                            }
                            printMainMenu();
                            break;
                        case "6":
                            System.out.println("你进入了：--> 备份通讯录 <-- 功能");
                            backupExit:
                            while (true) {
                                System.out.print("请输入备份地址（备份地址为本机的全路径地址，默认备份文件名为txl.csv。例如：D:\\）[0] 回到主菜单：");
                                while (scanner.hasNext()) {
                                    data = scanner.next();
                                    if ("0".equals(data)) {
                                        break backupExit;
                                    }
                                    String backupPath = data.replaceAll("\\\\", "/");
                                    backupPath = (backupPath.endsWith("/") ? backupPath : backupPath + "/") + "txl.csv";
                                    byte[] bytes = new byte[1024];
                                    try (FileInputStream fileInputStream = new FileInputStream(bookInfoPath)) {
                                        try (FileOutputStream fileOutputStream = new FileOutputStream(backupPath)) {
                                            int dataLength;
                                            while (-1 != (dataLength = fileInputStream.read(bytes))) {
                                                fileOutputStream.write(bytes, 0, dataLength);
                                            }
                                            fileOutputStream.flush();
                                        } catch (Exception e) {
                                            System.out.println("备份失败原因：" + e.getMessage() + " 。请重新输入！ ");
                                            break;
                                        }
                                    } catch (Exception e) {
                                        System.out.println("备份失败原因：" + e.getMessage() + " 。请重新输入！ ");
                                        break;
                                    }
                                    System.out.println("文件备份到：" + backupPath + " 完成！");
                                    break backupExit;
                                }
                            }
                            printMainMenu();
                            break;
                        default:
                            System.out.print(TIPS_1);
                            break;
                    }
                }
            }
        } finally {
            saveToFile(bookInfoPath);
            System.out.println("退出保存成功！");
        }
    }

    private static void saveToFile(String filePath) throws IOException {
        File currentFile = new File(filePath);
        if (!currentFile.exists()) {
            System.out.println("通讯录文件不存在，开始创建");
            if (currentFile.createNewFile()) {
                System.out.println("通讯录文件创建成功");
            }
        }
        if (currentFile.isFile()) {
            String fileName = currentFile.getName();
            String[] fileNameInfo = fileName.split("\\.");
            String bakFileName = fileNameInfo[0] + "-" + System.currentTimeMillis();
            String bakFileNameSuffix = fileNameInfo[1];
            String bakFilePath = currentFile.getParentFile().getCanonicalPath().replaceAll("\\\\", "/");
            bakFilePath = bakFilePath.endsWith("/") ? bakFilePath : bakFilePath + "/";
            if (currentFile.renameTo(new File(bakFilePath + bakFileName + "." + bakFileNameSuffix))) {
                if (!currentFile.exists() && currentFile.createNewFile()) {
                    if (!NAME_CARD_LIST.isEmpty()) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (NameCard nameCard : NAME_CARD_LIST) {
                            stringBuilder.append(nameCard.toString()).append("\n");
                        }
                        // 保存新的数据
                        try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(currentFile.toPath()), StringLengthUtil.DEFAULT_CHARSET_NAME))) {
                            bufferedWriter.write(stringBuilder.toString());
                            bufferedWriter.flush();
                        }
                    }
                    System.out.println("文件保存成功");
                    File[] files = new File(bakFilePath).listFiles((f) -> f.getName().startsWith(fileNameInfo[0]));
                    if (null != files && files.length > FILE_BACKUP_COUNT) {
                        List<File> fileList = Arrays.stream(files).sorted(Comparator.comparingLong(File::lastModified)).collect(Collectors.toList());
                        List<File> deleteFiles = fileList.subList(0, fileList.size() - FILE_BACKUP_COUNT);
                        for (File df : deleteFiles) {
                            if (df.delete()) {
                                System.out.println(df.getCanonicalPath() + " 过期备份文件删除成功");
                            }
                        }
                    }
                }
            }
        }
    }

    private static void printMainMenu() {
        System.out.println("-----功能列表-----");
        System.out.println(" [1] 通讯录列表");
        System.out.println(" [2] 新增联系人");
        System.out.println(" [3] 修改联系人");
        System.out.println(" [4] 删除联系人");
        System.out.println(" [5] 查询联系人");
        System.out.println(" [6] 备份通讯录");
        System.out.println(" [0] 退出");
        System.out.print("请输入对应功能编号：");
    }

    private static void printListMenu(int total, int totalPage, int currentPage, int pageSize) {
        String template = "共[%d]条，总[%d]页，当前第[%d]页，每页[%d]行\n操作编号说明: [1] 首页，[2] 下一页，[3] 上一页，[4] 尾页，[0] 回到主菜单";
        System.out.printf((template) + "%n", total, totalPage, currentPage, pageSize);
        System.out.print("请输入操作编号：");
    }

    private static void printAdd(Map<Integer, String> skipMap) {
        System.out.println("以下为选填项目：");
        for (Map.Entry<Integer, String> map : skipMap.entrySet()) {
            System.out.println(map.getKey() + ": " + map.getValue());
        }
        System.out.println(" 0: 取消输入并保存");
        System.out.print("请输入对应的编号：");
    }

    private static void printEditListMenu(int total, int totalPage, int currentPage, int pageSize) {
        String template = "共[%d]条，总[%d]页，当前第[%d]页，每页[%d]行\n操作编号说明: [1] 首页，[2] 下一页，[3] 上一页，[4] 尾页，[5] 进入编辑模式，[0] 回到主菜单";
        System.out.printf((template) + "%n", total, totalPage, currentPage, pageSize);
        System.out.print("请输入操作编号：");
    }

    private static void printEdit(Map<Integer, String> skipMap) {
        System.out.println("选择对应的项目编号进行编辑：");
        for (Map.Entry<Integer, String> map : skipMap.entrySet()) {
            System.out.println(map.getKey() + ": " + map.getValue());
        }
        System.out.println(" 0: 取消输入并保存");
        System.out.print("请输入对应的编号：");
    }

    private static void printDeleteListMenu(int total, int totalPage, int currentPage, int pageSize) {
        String template = "共[%d]条，总[%d]页，当前第[%d]页，每页[%d]行\n操作编号说明: [1] 首页，[2] 下一页，[3] 上一页，[4] 尾页，[5] 进入删除模式，[0] 回到主菜单";
        System.out.printf((template) + "%n", total, totalPage, currentPage, pageSize);
        System.out.print("请输入操作编号：");
    }

    private static void printQueryMenu() {
        System.out.println("查询操作说明：[1] 按照用户名查询，[2] 按照手机号码查询，支持模糊查询，[0] 回到主菜单");
        System.out.print("请输入操作编号：");
    }

    private static void printQueryListMenu(int total, int totalPage, int currentPage, int pageSize) {
        String template = "共[%d]条，总[%d]页，当前第[%d]页，每页[%d]行\n操作编号说明: [1] 首页，[2] 下一页，[3] 上一页，[4] 尾页，[5] 继续查询，[0] 回到主菜单";
        System.out.printf((template) + "%n", total, totalPage, currentPage, pageSize);
        System.out.print("请输入操作编号：");
    }

    private static <T> List<T> getPageData(List<T> dataList, int currentPage, int pageSize) {
        List<T> pageList = new ArrayList<>();
        if (!(null == dataList || dataList.isEmpty())) {
            int total = dataList.size();
            if (total > (currentPage * pageSize)) {
                pageList = dataList.subList((currentPage - 1) * pageSize, (currentPage * pageSize));
            } else {
                pageList = dataList.subList((currentPage - 1) * pageSize, total);
            }
        }
        return pageList;
    }

    public static void getNameCardList(String fileDataSource) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(fileDataSource)), StringLengthUtil.DEFAULT_CHARSET_NAME))) {
            String data = bufferedReader.readLine();
            while (null != data) {
                if (!data.startsWith("#")) {
                    String[] records = data.split(DATA_DEFAULT_DELIMITER, -1);
                    if (records.length >= 14) {
                        NAME_CARD_LIST.add(createNameCard(records));
                    }
                }
                data = bufferedReader.readLine();
            }
        }
    }

    private static NameCard createNameCard(String[] records) {
        NameCard nameCrad = new NameCard();
        for (int index = 0, size = records.length; index < size; index++) {
            String data = records[index];
            if (!(null == data || data.isEmpty())) {
                switch (index) {
                    case 0:
                        nameCrad.setUserName(data);
                        break;
                    case 1:
                        nameCrad.setMobile(data);
                        break;
                    case 2:
                        nameCrad.setPhone(data);
                        break;
                    case 3:
                        nameCrad.setAddress(data);
                        break;
                    case 4:
                        nameCrad.setImessage(data);
                        break;
                    case 5:
                        nameCrad.setCompany(data);
                        break;
                    case 6:
                        nameCrad.setPosts(data);
                        break;
                    case 7:
                        nameCrad.setNickName(data);
                        break;
                    case 8:
                        nameCrad.setGender(data);
                        break;
                    case 9:
                        nameCrad.setEmail(data);
                        break;
                    case 10:
                        nameCrad.setBirthday(data);
                        break;
                    case 11:
                        nameCrad.setSite(data);
                        break;
                    case 12:
                        nameCrad.setRelationship(data);
                        break;
                    case 13:
                        nameCrad.setRemark(data);
                        break;
                    default:
                        break;
                }
            }
        }
        return nameCrad;
    }

    private static void printInfo(List<NameCard> nameCards, boolean isEdit) throws IllegalAccessException {
        if (null == nameCards || nameCards.isEmpty()) {
            System.out.println("没有查询到通讯录信息。");
        } else {
            // 处理字段信息到列信息
            ColumnInfo[] columnInfos = new ColumnInfo[NameCard.class.getDeclaredFields().length];
            for (NameCard nameCard : nameCards) {
                Field[] fields = NameCard.class.getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    LabelName annotation = field.getAnnotation(LabelName.class);
                    int orderIndex = annotation.orderIndex();
                    String headerName = annotation.value();
                    Object objectValue = field.get(nameCard);
                    String value = "";
                    int maxLength = StringLengthUtil.calcStrLength(headerName);
                    if (null != objectValue) {
                        value = objectValue.toString();
                        int valueLength = StringLengthUtil.calcStrLength(value);
                        maxLength = valueLength > maxLength ? valueLength : maxLength;
                    }
                    ColumnInfo columnInfo = columnInfos[orderIndex - 1];
                    if (null == columnInfo) {
                        List<String> values = new ArrayList<>();
                        values.add(value);
                        columnInfos[orderIndex - 1] = new ColumnInfo(headerName, maxLength, values);
                    } else {
                        if (maxLength > columnInfo.getMaxLength()) {
                            columnInfo.setMaxLength(maxLength);
                        }
                        columnInfo.getValues().add(value);
                    }
                }
            }

            // 列转行信息
            int columnSplit = 6;
            int rowSize = nameCards.size();
            String[] rows = new String[rowSize + 1];
            for (int index = 0; index < rowSize; index++) {
                for (ColumnInfo columnInfo : columnInfos) {
                    // 处理header
                    if (index == 0) {
                        String headerName = rows[index];
                        if (null == headerName || headerName.isEmpty()) {
                            rows[index] = StringLengthUtil.right(isEdit ? "[-] " + columnInfo.getHeaderName() : columnInfo.getHeaderName(), columnInfo.getMaxLength() + columnSplit);
                        } else {
                            rows[index] = headerName + StringLengthUtil.right(columnInfo.getHeaderName(), columnInfo.getMaxLength() + columnSplit);
                        }
                    }
                    // 处理值
                    String value = rows[index + 1];
                    if (null == value || value.isEmpty()) {
                        rows[index + 1] = StringLengthUtil.right(isEdit ? "[" + (index + 1) + "] " + columnInfo.getValues().get(index) : columnInfo.getValues().get(index), columnInfo.getMaxLength() + columnSplit);
                    } else {
                        rows[index + 1] = value + StringLengthUtil.right(columnInfo.getValues().get(index), columnInfo.getMaxLength() + columnSplit);
                    }
                }
            }
            // 输出结果
            Arrays.stream(rows).forEach(System.out::println);
        }
    }

}