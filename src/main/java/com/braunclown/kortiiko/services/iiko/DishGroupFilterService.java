package com.braunclown.kortiiko.services.iiko;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Сервис, который позволяет выделить группу "Блюда" и её дочерние подгруппы из полного списка групп, полученного из iiko
 */
public class DishGroupFilterService {

    private List<GroupPrototype> fullList;
    private List<GroupPrototype> filteredGroups;

    /**
     * @param originalList Полный список групп, полученный из iiko
     * @return Группа "Блюда" и её дочерние подгруппы
     */
    public List<GroupPrototype> filterGroups(List<GroupPrototype> originalList) {
        fullList = originalList;
        filteredGroups = new ArrayList<>();
        GroupPrototype root = fullList.stream()
                .filter(group -> group.getName().equals("Блюда") && group.getParentIikoId() == null)
                .findFirst().orElseThrow();
        filteredGroups.add(root);
        addChildren(root.getIikoId());
        return filteredGroups;
    }

    /**
     * Добавляет в список filteredGroups все дочерние группы, в том числе транзитивно дочерние
     * @param parentId Идентификатор родительской группы из iiko
     */
    private void addChildren(String parentId) {
        for (GroupPrototype groupPrototype: fullList) {
            if (Objects.equals(groupPrototype.getParentIikoId(), parentId)) {
                filteredGroups.add(groupPrototype);
                addChildren(groupPrototype.getIikoId());
            }
        }
    }
}
