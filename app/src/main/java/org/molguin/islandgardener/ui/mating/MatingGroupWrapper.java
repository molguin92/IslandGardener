/*
 *     Copyright © 2020 Manuel Olguín Muñoz <manuel@olguin.se>
 *
 *     MatingGroupAdapter.java is part of Island Gardener
 *
 *     Island Gardener is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Island Gardener is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Island Gardener.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.molguin.islandgardener.ui.mating;

import com.xwray.groupie.Group;
import com.xwray.groupie.GroupAdapter;

import org.molguin.islandgardener.flowers.FuzzyFlower;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MatingGroupWrapper {

    private final GroupAdapter adapter;
    private final List<FuzzyFlower> flowers;
    private boolean advancedMode;
    private boolean invWGeneMode;

    public MatingGroupWrapper(boolean advancedMode, boolean invWGeneMode) {
        super();
        this.advancedMode = advancedMode;
        this.invWGeneMode = invWGeneMode;
        this.adapter = new GroupAdapter();
        this.flowers = new ArrayList<FuzzyFlower>();
    }

    public GroupAdapter getAdapter() {
        return this.adapter;
    }

    public void setFlowers(Collection<FuzzyFlower> flowers) {
        this.flowers.clear();
        this.flowers.addAll(flowers);
        this.update();
    }

    private void update() {
        List<Group> newGroups = new ArrayList<Group>(flowers.size());

        if (this.advancedMode)
            for (FuzzyFlower f : this.flowers)
                newGroups.add(new ExpandableFlowerGroup(f, this.invWGeneMode));
        else
            for (FuzzyFlower f : this.flowers)
                newGroups.add(new SimpleFlowerItem(f));

        this.adapter.updateAsync(newGroups);
    }

    void setAdvancedMode(boolean on) {
        if (on == this.advancedMode) return;
        this.advancedMode = on;
        this.update();
    }

    void setInvWGeneMode(boolean on) {
        if (on == this.invWGeneMode) return;
        this.invWGeneMode = on;
        this.update();
    }

}
