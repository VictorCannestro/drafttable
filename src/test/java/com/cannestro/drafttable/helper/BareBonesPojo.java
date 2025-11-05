package com.cannestro.drafttable.helper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Setter
@Getter
@AllArgsConstructor
public class BareBonesPojo {

    private String name;
    private List<Integer> numberOfBones;

}
