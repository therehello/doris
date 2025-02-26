// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.doris.nereids.analyzer;

import org.apache.doris.nereids.exceptions.UnboundException;
import org.apache.doris.nereids.memo.GroupExpression;
import org.apache.doris.nereids.properties.LogicalProperties;
import org.apache.doris.nereids.trees.expressions.Expression;
import org.apache.doris.nereids.trees.expressions.NamedExpression;
import org.apache.doris.nereids.trees.expressions.Slot;
import org.apache.doris.nereids.trees.plans.BlockFuncDepsPropagation;
import org.apache.doris.nereids.trees.plans.Plan;
import org.apache.doris.nereids.trees.plans.PlanType;
import org.apache.doris.nereids.trees.plans.algebra.InlineTable;
import org.apache.doris.nereids.trees.plans.logical.LogicalLeaf;
import org.apache.doris.nereids.trees.plans.visitor.PlanVisitor;
import org.apache.doris.nereids.util.Utils;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** UnboundInlineTable */
public class UnboundInlineTable extends LogicalLeaf implements InlineTable, BlockFuncDepsPropagation, UnboundPlan {
    private final List<List<NamedExpression>> constantExprsList;

    public UnboundInlineTable(List<List<NamedExpression>> constantExprsList) {
        super(PlanType.LOGICAL_UNBOUND_INLINE_TABLE, Optional.empty(), Optional.empty());
        this.constantExprsList = Utils.fastToImmutableList(
                Objects.requireNonNull(constantExprsList, "constantExprsList can not be null")
        );
    }

    public List<List<NamedExpression>> getConstantExprsList() {
        return constantExprsList;
    }

    @Override
    public <R, C> R accept(PlanVisitor<R, C> visitor, C context) {
        return visitor.visitUnboundInlineTable(this, context);
    }

    @Override
    public List<? extends Expression> getExpressions() {
        ImmutableList.Builder<Expression> expressions = ImmutableList.builderWithExpectedSize(
                constantExprsList.size() * constantExprsList.get(0).size());

        for (List<NamedExpression> namedExpressions : constantExprsList) {
            expressions.addAll(namedExpressions);
        }

        return expressions.build();
    }

    @Override
    public Plan withGroupExpression(Optional<GroupExpression> groupExpression) {
        return this;
    }

    @Override
    public Plan withGroupExprLogicalPropChildren(Optional<GroupExpression> groupExpression,
            Optional<LogicalProperties> logicalProperties, List<Plan> children) {
        return this;
    }

    @Override
    public List<Slot> computeOutput() {
        throw new UnboundException("output");
    }
}
