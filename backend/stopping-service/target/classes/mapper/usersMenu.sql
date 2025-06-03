-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('用户表', '3', '1', 'users', 'users/users/index', 1, 0, 'C', '0', '0', 'users:users:list', '#', 'admin', sysdate(), '', null, '用户表菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('用户表查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'users:users:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('用户表新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'users:users:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('用户表修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'users:users:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('用户表删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'users:users:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('用户表导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'users:users:export',       '#', 'admin', sysdate(), '', null, '');