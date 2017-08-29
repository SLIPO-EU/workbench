import * as React from 'react';
import { Container, Row, Col, Button } from 'reactstrap';
import { NavLink } from 'react-router-dom';

export default class Page403 extends React.Component {
  render() {
    return (
      <div className='app flex-row align-items-center'>
        <Container>
          <Row className='justify-content-center'>
            <Col md='6'>
              <div className='clearfix'>
                <h1 className='float-left display-3 mr-4'>403</h1>
                <h4 className='pt-3'>Oops! You shouldn't be here.</h4>
                <p className='text-muted float-left'>The page you are looking for was not found or you do not have the required permission.</p>
              </div>
              <NavLink to='/dashboard'>
                <Button color='danger'><i className='fa fa-home'></i>&nbsp; Take Me Home</Button>
              </NavLink>
            </Col>
          </Row>
        </Container>
      </div>
    );
  }
}
