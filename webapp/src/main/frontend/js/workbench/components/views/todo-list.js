var React = require('react');

const TodoList = ({ todos }) => (
  <div>
    <h4>Todo List</h4>
    <ul>
      {todos.map((todo) => (
        <li
          id={'todo-' + todo.id}
          key={todo.id}
        >{todo.text}</li>
      ))}
    </ul>
  </div>
);

module.exports = TodoList;
