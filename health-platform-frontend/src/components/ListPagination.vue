<template>
    <div v-if="total > 0" class="list-pagination">
        <el-pagination
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            :current-page="page"
            :page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            @current-change="(p) => emit('change', p, pageSize)"
            @size-change="(s) => emit('change', 1, s)"
        />
    </div>
</template>

<script setup>
//通用列表分页条：受控组件，页码/页大小由父组件管理，变化时抛 change(page, pageSize)
//切换每页条数时自动回到第 1 页
defineProps({
    total: { type: Number, default: 0 },
    page: { type: Number, default: 1 },
    pageSize: { type: Number, default: 10 }
})
const emit = defineEmits(['change'])
</script>

<style scoped>
.list-pagination {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
}
</style>
